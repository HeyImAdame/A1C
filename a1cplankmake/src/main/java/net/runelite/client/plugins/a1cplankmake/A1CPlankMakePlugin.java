package net.runelite.client.plugins.a1cplankmake;

import javax.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import java.util.Arrays;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "A1C Plank Make",
        description = "Have coins/runes/butler/jewelry box setup",
        tags = {"one", "click", "plank", "adam", "construction"},
        enabledByDefault = false
)
public class A1CPlankMakePlugin extends Plugin
{
    @Inject
    private Client client;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private A1CPlankMakeConfig config;
    @Provides
    A1CPlankMakeConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(A1CPlankMakeConfig.class);
    }
    private int timeout = 0;
    private String action;
    private int logID = 0;
    private int plankID = 0;
    private int bankID = 0;
    private int forcelogout = 0;
    private int stuckCounter = 0;
    private String lastaction;

    @Override
    protected void startUp() throws Exception {
        timeout = 0;
        action = "";
        logID = 0;
        plankID = 0;
        bankID = 0;
        forcelogout = 0;
        stuckCounter = 0;
        updateConfig();
    }
    @Override
    protected void shutDown() throws Exception {
        updateConfig();
        timeout = 0;
        action = "";
        logID = 0;
        plankID = 0;
        bankID = 0;
        forcelogout = 0;
        stuckCounter = 0;
    }
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        updateConfig();
    }
    @Subscribe
    public void OnGameTick(GameTick event) {
        if (logID == 0 || plankID == 0 || bankID == 0)
        {
            updateConfig();
        }
        if (timeout > 0)
        {
            timeout--;
        }
        if (timeout == 50)
        {
            timeout = 0;
            logout();
            return;
        }
    }
    @Subscribe
    public void onClientTick(ClientTick event) {
        if (client.getLocalPlayer() == null
                || client.getGameState() != GameState.LOGGED_IN
                || client.getWidget(378, 78) != null)
        {
            return;
        }
        if (!(isInPOH() || isAtBank())) {return;}
        String text;
            text =  "<col=00ff00>Adam Board Generator";

        client.insertMenuItem(
                text,
                "",
                MenuAction.UNKNOWN.getId(),
                0,
                0,
                0,
                true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x -> x.getOption().equals(text)).findFirst().orElse(null));
    }
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (event.getMenuOption().equals("<col=00ff00>Adam Board Generator")) {
            if (isbankOpen()) {
                if (client.getVarbitValue(6590) != 0) {
                    event.setMenuEntry(createMenuEntry(1, MenuAction.CC_OP, -1, 786460, false));
                    action = "setwithdrawOpt";
                    return;
                }
                if (client.getVarbitValue(Varbits.CURRENT_BANK_TAB) != 0) {
                    event.setMenuEntry(createMenuEntry(1, MenuAction.CC_OP, 10, WidgetInfo.BANK_TAB_CONTAINER.getId(), false));
                    action = "setbanktab";
                    return;
                }
            } //set bank options
            if (shouldConsume()) {
                System.out.println("Consumed. Timeout = " + timeout);
                event.consume();
                return;
            }
            if (!hasItems()) {
                sendGameMessage("Missing items. Need at least 50k and law runes.");
                event.consume();
                return;
            }
            if (!goodSpellbook()) {
                sendGameMessage("Wrong spellbook. Must be on standard.");
                event.consume();
                return;
            }
            if (stuckCounter > 10) {
                sendGameMessage("No more materials found. Logging out in 15 seconds.");
                timeout = 75;
                return;
            }
                lastaction = action;
                handleClick(event);
                debug();
            if (checkifStuck()) {
                stuckCounter = stuckCounter + 1;
                return;
            }
            stuckCounter = 0;
        }
    }

    //Handle clicks
    private void handleClick(MenuOptionClicked event) {
        if (isAtBank()) {
            handleBankClick(event);
            return;
        }
        if (isInPOH()) {
            handlePOHClick(event);
            return;
        }
        action = "idle";
    }
    private void handleBankClick(MenuOptionClicked event) {
        if (getEmptySlots() == 0 && (countInvIDs(logID) > 0)) {
            event.setMenuEntry(teleportToHouseMES());
            timeout = 7;
            action = "House tele";
            return;
        }
        if (!isbankOpen()) {
            event.setMenuEntry(openBank());
            timeout = 5;
            action = "Open bank";
            return;
        }
//        if (client.getVarbitValue(6590) != 0) {
//            event.setMenuEntry(createMenuEntry(1, MenuAction.CC_OP, -1, 786460, false));
//            action = "setwithdrawOpt";
//            return;
//        }
//        if (client.getVarbitValue(Varbits.CURRENT_BANK_TAB) != 0) {
//            event.setMenuEntry(createMenuEntry(1, MenuAction.CC_OP, 10, WidgetInfo.BANK_TAB_CONTAINER.getId(), false));
//            action = "setbanktab";
//            return;
//        }
        if (countInvIDs(plankID) > 0) {
            event.setMenuEntry(depositPlanks());
            timeout = 1;
            action = "Depositing planks";
            return;
        }
        if (outofMaterials()) {
            forcelogout = 1;
            action = "outofMaterials";
            return;
        }
        event.setMenuEntry(withdrawItem(logID));
        timeout = 1;
        action = "Withdraw item";
        return;
    }
    private void handlePOHClick(MenuOptionClicked event) {
        if (!client.getWidget(162, 42).isHidden()) {
            sendGameMessage("Butler not setup to take x26 to the sawmill.");
            forcelogout = 1;
            return;
        } //check butler setup
        if (client.getWidget(219, 1) != null) {
            if (!client.getWidget(WidgetInfo.DIALOG_OPTION_OPTION1).getChild(3).getText().contains("sawmill")) {
                event.setMenuEntry(sendToSawmillMES(1));
                timeout = 1;
                action = "SendToSawmill";
                return;
            }
            sendGameMessage("Butler not setup to take x26 to the sawmill.");
            forcelogout = 1;
            return;
            //event.setMenuEntry(useLogsOnNPC());
            //timeout = 1;
            //action = "UseLogsOnNPC";
            //return;
        }
        if (client.getWidget(231, 5) != null) {
            event.setMenuEntry(clickContinueMES());
            timeout = 1;
            action = "clickContinue";
            return;
        }
        if (action == "callButler") {
            event.setMenuEntry(clickButler());
            timeout = 1;
            action = "clickButlerclicker";
            return;
        }
        if (client.getWidget(370, 19) != null && client.getWidget(370, 19).getChild(3) != null) {
            event.setMenuEntry(callButlerMES());
            timeout = 1;
            action = "callButler";
            return;
        }
        if (countInvIDs(logID) > 0
                && client.getWidget(116, 8) != null) {
            event.setMenuEntry(houseOptionsMES());
            timeout = 1;
            action = "houseOpts";
            return;
        }
        event.setMenuEntry(teleToBank());
        timeout = 5;
        action = "teletobank";
        return;
    }

    //SUBROUTINES
    private void updateConfig() {
        bankID = config.bank().ID;
        logID = config.plank().ID1;
        plankID = config.plank().ID2;
        action = "";
        forcelogout = 0;
        return;
    }
    private Widget getInventoryItem(int id) {
        client.runScript(6009, 9764864, 28, 1, -1); //rebuild inventory ty pajeet
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (bankInventoryWidget != null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget, id);
        }
        if (inventoryWidget != null) //if hidden check exists then you can't access inventory from any tab except inventory
        {
            return getWidgetItem(inventoryWidget, id);
        }
        return null;
    }
    private long countInvIDs(Integer id) {
        if (isbankOpen()) {
            List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getChildren());
            return (inventoryWidget.stream().filter(item -> item.getItemId() == id).count());
        }
        List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getChildren());
        return (inventoryWidget.stream().filter(item -> item.getItemId() == id).count());
    }
    private int getEmptySlots() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());
        if (inventory != null && !inventory.isHidden()
                && inventory.getDynamicChildren() != null) {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        if (bankInventory != null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren() != null) {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }
        return -1;
    }

    //ACTIONS
    private MenuEntry houseOptionsMES() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                7602250,
                false);
    }
    private MenuEntry callButlerMES() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                24248339,
                false);
    }
    private MenuEntry clickButler() {
        NPC npc = getNpc(229);
        return createMenuEntry(
                npc.getIndex(),
                MenuAction.NPC_FIRST_OPTION,
                getNPCLocation(npc).getX(),
                getNPCLocation(npc).getY(),
                false);
    }
    private MenuEntry clickContinueMES() {
        return createMenuEntry(
                0,
                MenuAction.WIDGET_CONTINUE,
                -1,
                15138821,
                false);
    }
    private MenuEntry sendToSawmillMES(int chatOpt) {
        return createMenuEntry(
                0,
                MenuAction.WIDGET_CONTINUE,
                chatOpt,
                WidgetInfo.DIALOG_OPTION_OPTION1.getId(),
                false);
    }
    private MenuEntry teleToBank() {
        GameObject JewelryBox = getGameObject(29156);
        if (config.bank() == Types.Bank.CWars) {
            return createMenuEntry(JewelryBox.getId(),
                    MenuAction.GAME_OBJECT_THIRD_OPTION,
                    getLocation(JewelryBox).getX(),
                    getLocation(JewelryBox).getY(),
                    false);
        }
        return client.createMenuEntry(
                "Seers'",
                "Camelot Teleport",
                2,
                MenuAction.CC_OP.getId(),
                -1,
                WidgetInfo.SPELL_TELEOTHER_CAMELOT.getId(),
                true);
    }
    private MenuEntry openBank() {
        GameObject gameObject = getGameObject(config.bank().ID);
        if (config.bank().Type == "Booth") {
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_SECOND_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    false);
        }
        if (config.bank().Type == "Chest") {
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    false);
        }
        return null;
    }
    private MenuEntry depositPlanks() {
        Widget item1 = getInventoryItem(plankID);
        if (item1 == null) {
            return null;
        }
        return createMenuEntry(
                8,
                MenuAction.CC_OP_LOW_PRIORITY,
                item1.getIndex(),
                983043,
                false);
    }
    private MenuEntry withdrawItem(Integer configID) {
        if (getBankIndex(configID) == -1) {
            return null;
        }
        return createMenuEntry(
                7,
                MenuAction.CC_OP,
                getBankIndex(configID),
                786445,
                false);
    }
    private MenuEntry teleportToHouseMES() {
        if (getInventoryItem(ItemID.CONSTRUCT_CAPE) != null) {
            return createMenuEntry(ItemID.CONSTRUCT_CAPE,
                    MenuAction.ITEM_FOURTH_OPTION,
                    getInventoryItem(ItemID.CONSTRUCT_CAPE).getIndex(),
                    WidgetInfo.INVENTORY.getId(),
                    false);
        }
        if (getInventoryItem(ItemID.CONSTRUCT_CAPET) != null) {
            return createMenuEntry(ItemID.CONSTRUCT_CAPET,
                    MenuAction.ITEM_FOURTH_OPTION,
                    getInventoryItem(ItemID.CONSTRUCT_CAPET).getIndex(),
                    WidgetInfo.INVENTORY.getId(),
                    false);
        }
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                WidgetInfo.SPELL_TELEPORT_TO_HOUSE.getId(),
                false);
    }
//    private MenuEntry useJewelryBoxMES() {
//        GameObject JewelryBox = getGameObject(29156);
//        return createMenuEntry(JewelryBox.getId(),
//                MenuAction.GAME_OBJECT_THIRD_OPTION,
//                getLocation(JewelryBox).getX(),
//                getLocation(JewelryBox).getY(),
//                true);
//    }

    //BOOLEANS
    private boolean isAtBank() {
        return getGameObject(bankID) != null;
    }
    private boolean isInPOH() {
        return getGameObject(4525) != null; //checks for portal, p sure this is same for everyone if not need to do alternative check.
    }
    private boolean isbankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }
    private boolean hasItems() {
        if (getInventoryItem(995) != null
                && getInventoryItem(563) != null) {
            int coinInd = getInventoryItem(995).getIndex();
            int coinAmt = client.getWidget(WidgetInfo.INVENTORY).getChild(coinInd).getItemQuantity();
            int teleInd = getInventoryItem(563).getIndex();
            int teleAmt = client.getWidget(WidgetInfo.INVENTORY).getChild(teleInd).getItemQuantity();
            if (coinAmt > 50000 && teleAmt > 1) {
                return true;
            }
        }
        return false;
    }
    private boolean goodSpellbook() {
        if (WidgetInfo.SPELLBOOK.getId() == 14286848)
        {
            return true;
        }
        return false;
    }
    private boolean checkifStuck() {
        return (lastaction == action);
    }
    private boolean outofMaterials() {
        return (getBankIndex(logID) == -1
                && isbankOpen()
                && getEmptySlots() > 7);
    }
    private boolean shouldConsume() {
        if (!config.consumeMisclicks()) {
            return false;
        }
        return (client.getLocalPlayer().getAnimation() != -1
                || timeout > 0
                || client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null);
    }

    //EXTRAS
    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
    private NPC getNpc(int id) {
        return new NPCQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    private Point getNPCLocation(NPC npc) {
        return new Point(npc.getLocalLocation().getSceneX(), npc.getLocalLocation().getSceneY());
    }
    private Point getLocation(TileObject tileObject) {
        if (tileObject instanceof GameObject)
        {

            return ((GameObject) tileObject).getSceneMinLocation();
        }
        else
        {
            return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
        }
    }
    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    private int getBankIndex(int id) {
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(id)
                .result(client)
                .first();
        if (bankItem == null)
        {
            return -1;
        }
        return bankItem.getWidget().getIndex();
    }
    private void setSelectedInventoryItem(Widget item) {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getItemId());
    }
    private Widget getWidgetItem(Widget widget, int id) {
        for (Widget item : widget.getDynamicChildren())
        {
            if (item.getItemId() == id)
            {
                return item;
            }
        }
        return null;
    }
    private void sendGameMessage(String message) {
        String chatMessage = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(message)
                .build();
        chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
    }
    private void debug() {
        System.out.println("action = " + action + " timeout = " + timeout + " shouldconsume = " + shouldConsume());
    }
    private void logout() {
        if (client.getWidget(69, 23) != null)
        {
            client.invokeMenuAction("Logout", "", 1, MenuAction.CC_OP.getId(), -1, WidgetInfo.WORLD_SWITCHER_LOGOUT_BUTTON.getId());
        }
        else
        {
            client.invokeMenuAction("Logout", "", 1, MenuAction.CC_OP.getId(), -1, 11927560);
        }
    }
    private void enterPlankstoMake() {
        //client.invokeMenuAction();
        //getWidget(WidgetInfo.CHATBOX_FULL_INPUT);
        //client.getWidget(162, 42);

        return;
    }
    private MenuEntry useLogsOnNPC() {
        NPC npc = getNpc(229);
        Widget logItem = getInventoryItem(logID);
        setSelectedInventoryItem(logItem);
        return client
                .createMenuEntry(1)
                .setOption("Use")
                .setTarget("<col=ff9040>Mahogany logs</col><col=ffffff> -> <col=ffff00>Demon butler")
                .setIdentifier(npc.getIndex())
                .setType(MenuAction.WIDGET_TARGET_ON_NPC)
                .setParam0(0)
                .setParam1(0)
                .setForceLeftClick(true);
    }
}