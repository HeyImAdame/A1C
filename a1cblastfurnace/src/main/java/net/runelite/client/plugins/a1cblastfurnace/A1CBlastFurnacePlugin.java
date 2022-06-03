package net.runelite.client.plugins.a1cblastfurnace;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.rs.api.RSClient;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static net.runelite.api.NullObjectID.NULL_9092;
import static net.runelite.api.ObjectID.CONVEYOR_BELT;
import static net.runelite.api.Varbits.BLAST_FURNACE_COFFER;

@Extension
@PluginDescriptor(
        name = "A1C Blast Furnace",
        description = "Sick bars bro",
        enabledByDefault = false,
        tags = {"a1c","one","click","oneclick","smithing","blast","furnace"})
@Slf4j
public class A1CBlastFurnacePlugin extends Plugin {

    private boolean coalBagFull = false;
    private int waitBars;
    private int timeout = 0;
    private String action;
    private String lastaction;
    private int stuckCounter = 0;
    private int cachedXP;
    private int dispenserState;
    private boolean xpDrop;
    private static final int BAR_DISPENSER = NULL_9092;
    @Getter(AccessLevel.PACKAGE)
    private GameObject barDispenser;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private Client client;
    @Inject
    private A1CBlastFurnaceConfig config;
    @Provides
    A1CBlastFurnaceConfig providesConfig(ConfigManager configManager) {
        return configManager.getConfig(A1CBlastFurnaceConfig.class);
    }
    @Override
    protected void startUp() throws Exception {
        coalBagFull = false;
        barDispenser = null;
        waitBars = 0;
        timeout = 0;
        stuckCounter = 0;
        xpDrop = false;
        cachedXP = client.getSkillExperience(Skill.SMITHING);
    }
    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        GameObject gameObject = event.getGameObject();

        switch (gameObject.getId())
        {
            case BAR_DISPENSER:
                dispenserState = client.getVarbitValue(Varbits.BAR_DISPENSER);
                barDispenser = gameObject;
                break;
        }
    }
    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        GameObject gameObject = event.getGameObject();

        switch (gameObject.getId())
        {
            case BAR_DISPENSER:
                barDispenser = null;
                break;
        }
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getItemContainer() == client.getItemContainer(InventoryID.INVENTORY))
        {
            if (getInventoryItem(config.barType().getBarID()) != null) {
                waitBars = 0;
            }
        }
    }
    @Subscribe
    protected void onStatChanged(StatChanged event) {
        //on login this method triggers going from 0 to players current XP. all xp drops(even on leagues etc) should be below 50k and this method requires 77 rc.
        if (event.getSkill() == Skill.SMITHING && event.getXp()- cachedXP <50000) {
            xpDrop = true;
            waitBars = 1;
        }
        cachedXP = client.getSkillExperience(Skill.SMITHING);
    }
    @Subscribe
    public void onGameTick(GameTick event) {
        if (timeout > 0) timeout--;
        if (timeout == 50) {
            logout();
            return;
        }
        if (shouldResetTimeout()) {
            timeout = 0;
        }
    }
    @Subscribe
    public void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null
                || this.client.getGameState() != GameState.LOGGED_IN
                || client.getWidget(378, 78) != null)
        {
            return;
        }

        String text = "<col=00ff00>One Click Blast Furnace";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
        //Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (event.getMenuOption().equals("<col=00ff00>One Click Blast Furnace")) {
            lastaction = action;
            int lasttimeout = timeout;

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
            if ((stuckCounter > 6
                    || outofMaterials(config.barType().getOreID()))
                    && timeout < 50) {
                sendGameMessage("Stuck on step " + action + ". Logging out in 15 seconds.");
                timeout = 75;
                event.consume();
                return;
            }
            handleClick(event);
            debug(lasttimeout);
            if (checkifStuck()) {
                stuckCounter = stuckCounter + 1;
                return;
            }
            stuckCounter = 0;
        }
    }

    //Handle Clicks
    private void handleClick(MenuOptionClicked event) {
        if (!isCofferMoney()) {
            sendGameMessage("No GP in coffer you broke bitch");
            System.out.println("No more GP");
            return;
        }

        if (config.barType() == A1CBlastFurnaceTypes.GOLD) {
            if (xpDrop && action != "equipIceGloves"
                    && hasIceGloves()) {
                event.setMenuEntry(equipIceMES());
                action = "equipIceGloves";
                xpDrop = false;
                timeout = 0;
                return;
            } else if (hasGoldGloves()
                    && action != "equipGoldGloves"
                    && waitBars == 0) {
                event.setMenuEntry(equipGoldGlovesMES());
                action = "equipGoldGloves";
                timeout = 0;
                return;
            }
        } //equip gloves

        if (oreInInvent() || coalInInvent()) { //if ore or coal in invent should be depositing.
            event.setMenuEntry(depositOreMES());
            action = "depositOre";
            timeout = 12;
            return;
        } //depositbelt

        if (coalBagFull && getEmptySlots() > 1 && action == "depositOre") {
            event.setMenuEntry(emptyCoalBagMES());
            action = "emptyBag";
            coalBagFull = false;
            timeout = 2;
            return;
        } //empty coalbag

        if (waitBars == 1
                && getEmptySlots() > 25) {
            switch (getDispenserState()) {
                case 0:
                    if (waitBars == 1
                            && !isatBarsTile()) {
                        walktoBarPickup();
                        action = "walktobarstile";
                        timeout = 12;
                        return;
                    }
                case 2:
                    if (client.getWidget(270,1) != null
                            && action != "takeBars") {
                        event.setMenuEntry(takeBarsMES());
                        action = "takeBars";
                        //waitBars = 0;
                    } else { //if () {
                        event.setMenuEntry(withdrawBarsMES());
                        action = "withdrawBars";
                    }
                    timeout = 1;
                    return;
                case 3:
                    if (client.getWidget(270,1) != null
                            && action != "takeBars") {
                        event.setMenuEntry(takeBarsMES());
                        action = "takeBars";
                        //waitBars = 0;
                        timeout = 1;
                        return;
                    }
                    event.setMenuEntry(withdrawBarsMES());
                    action = "withdrawBars";
                    timeout = 1;
                    return;
            }
        } //handle bar dispenser

        if (isbankOpen()){
            handleBankClick(event);
            return;
        } //bank clickers

        if (getInventoryItem(config.barType().getBarID()) != null
                || (getInventoryItem(config.barType().getOreID()) == null && waitBars == 0)
                || (getInventoryItem(config.barType().getCoal()) == null && waitBars == 0)) {
            event.setMenuEntry(bankMES());
                action = "openBank";
                timeout = 12;
                return;
        }
        action = "idle";
    }
    private void handleBankClick(MenuOptionClicked event) {
        ImmutableList<Integer> StaminaIds = ImmutableList.of(
                ItemID.STAMINA_POTION1,
                ItemID.STAMINA_POTION2,
                ItemID.STAMINA_POTION3,
                ItemID.STAMINA_POTION4);
        for (Integer staminaID : StaminaIds) {
            if (getInventoryItem(staminaID) != null
                    && action != "drinkStam") {
                event.setMenuEntry(drinkStamMES(staminaID));
                timeout = 1;
                action = "drinkStam";
                return;
            }
        }
        if ((client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0
                && client.getEnergy() < config.drinkbelow())
                || client.getEnergy() < 20) {
            if (getEmptySlots() < 1
                    && action != "depositProds") {
                event.setMenuEntry(depositProds());
                timeout = 0;
                action = "depositProds";
                return;
            } else if (action != "withdrawStam")
            event.setMenuEntry(withdrawStaminaMES());
            timeout = 1;
            action = "withdrawStam";
            return;
        }

        if (getInventoryItem(config.barType().getBarID()) != null
                && action != "depositProds") {
            event.setMenuEntry(depositProds());
            action = "depositProds";
            timeout = 0;
            return;
        }

        if (!coalBagFull && fillCoalBagMES() != null
                && action != "fillCoalBag") {
            event.setMenuEntry(fillCoalBagMES());
            coalBagFull = true;
            action = "fillCoalBag";
            timeout = 0;
            return;
        }
        int coalDeposited = client.getVarbitValue(Varbits.BLAST_FURNACE_COAL);
        A1CBlastFurnaceTypes Bar = config.barType();
        if (Bar.getCoal() * 28 > coalDeposited && getEmptySlots() > 10
                && action != "withdrawCoal") { //this is overkill but can afford to be without any negatives afaik. Need surplus always to prevent iron bars accidentally. Covers with and without coal bag.
            event.setMenuEntry(withdrawCoalMES());
            action = "withdrawCoal";
            timeout = 2;
            return;
//        } else if (coalInInvent() && coalBagFull
//                && action != "depositOre") {
//            event.setMenuEntry(depositOreMES());
//            action = "depositOre";
//            waitBars = 1;
//            timeout = 12;
//            return;
        } else { //withdraw coal cooldown somehow needed, think it's due to leaving bank before shits loaded? idfk
            event.setMenuEntry(withdrawOreMES());
            action = "withdrawOre";
            timeout = 2;
            return;
        }
    }

    //SUBROUTINES
    private int getEmptySlots() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());

        if (inventory!=null && !inventory.isHidden()
                && inventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        if (bankInventory!=null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }
        return -1;
    }
    private int getBankIndex(int ID){
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(ID)
                .result(client)
                .first();
        if (bankItem != null) {
            return bankItem.getWidget().getIndex();
        }
        return -1;
    }
    private int getDispenserState() {return client.getVarbitValue(Varbits.BAR_DISPENSER);}

    //ACTIONS
    private MenuEntry withdrawOreMES() {
        A1CBlastFurnaceTypes Ore = config.barType();
        return createMenuEntry(7,
                MenuAction.CC_OP_LOW_PRIORITY,
                getBankIndex(Ore.getOreID()),
                WidgetInfo.BANK_ITEM_CONTAINER.getId(),
                false);
    }
    private MenuEntry withdrawCoalMES() {
        int coal = 453;
        return createMenuEntry(7,
                MenuAction.CC_OP_LOW_PRIORITY,
                getBankIndex(coal),
                WidgetInfo.BANK_ITEM_CONTAINER.getId(),
                false);
    }
    private MenuEntry withdrawStaminaMES() {
        int staminaDose = 12631;
        return createMenuEntry(1,
                MenuAction.CC_OP,
                getBankIndex(staminaDose),
                WidgetInfo.BANK_ITEM_CONTAINER.getId(),
                false);
    }
    private MenuEntry withdrawBarsMES() {
        GameObject barDispenser = getGameObject(9092);
        return createMenuEntry(barDispenser.getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(barDispenser).getX(),
                getLocation(barDispenser).getY(),
                false);
    }
    private MenuEntry emptyCoalBagMES() {
        Widget closedCoalBag = getInventoryItem(12019);
        Widget openCoalBag = getInventoryItem(24480);

        coalBagFull = false;
        if (closedCoalBag != null) {
            return createMenuEntry(6,
                    MenuAction.CC_OP_LOW_PRIORITY,
                    closedCoalBag.getIndex(),
                    WidgetInfo.INVENTORY.getId(),
                    false);
        }
        if (openCoalBag != null) {
            return createMenuEntry(6,
                    MenuAction.CC_OP_LOW_PRIORITY,
                    openCoalBag.getIndex(),
                    WidgetInfo.INVENTORY.getId(),
                    false);
        }
        return null;
    }
    private MenuEntry equipGoldGlovesMES() {
        int gloveID = 776;
        Widget goldGloves = getInventoryItem(gloveID);
        if (goldGloves != null) {
            return createMenuEntry(3,
                    MenuAction.CC_OP,
                    goldGloves.getIndex(),
                    WidgetInfo.INVENTORY.getId(),
                    false);
        }
        return null;
    }
    private MenuEntry equipIceMES() {
        int gloveID = 1580;
        Widget iceGloves = getInventoryItem(gloveID);
        if (iceGloves != null) {
            return createMenuEntry(3,
                    MenuAction.CC_OP,
                    iceGloves.getIndex(),
                    WidgetInfo.INVENTORY.getId(),
                    false);
        }
        return null;
    }
    private MenuEntry depositOreMES() {
        GameObject belt = getGameObject(9100);
        return createMenuEntry(belt.getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(belt).getX(),
                getLocation(belt).getY(),
                false);
    }
    private MenuEntry takeBarsMES() {
        return createMenuEntry(1,
                MenuAction.CC_OP,
                -1,
                17694734,
                false);
    }
    private MenuEntry drinkStamMES(int id) {
        Widget staminaDose = getInventoryItem(id);
        return createMenuEntry(9,
                MenuAction.CC_OP_LOW_PRIORITY,
                staminaDose.getIndex(),
                WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(),
                false);
    }
    private MenuEntry bankMES() {
        GameObject bank = getGameObject(26707);
        return createMenuEntry(bank.getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(bank).getX(),
                getLocation(bank).getY(),
                false);
    }
    private MenuEntry depositProds() {
        if (getInventoryItem(config.barType().getBarID()) != null) {
            return createMenuEntry(8,
                    MenuAction.CC_OP_LOW_PRIORITY,
                    getInventoryItem(config.barType().getBarID()).getIndex(),
                    983043,
                    false);
        }
        if (getInventoryItem(config.barType().getOreID()) != null) {
            return createMenuEntry(8,
                    MenuAction.CC_OP_LOW_PRIORITY,
                    getInventoryItem(config.barType().getOreID()).getIndex(),
                    983043,
                    false);
        }
        return null;
    }
    private MenuEntry fillCoalBagMES() {
        Widget closedCoalBag = getInventoryItem(12019);
        Widget openCoalBag = getInventoryItem(24480);
        coalBagFull = true;
        if (closedCoalBag!=null) {
            return createMenuEntry(9,
                    MenuAction.CC_OP_LOW_PRIORITY,
                    closedCoalBag.getIndex(),
                    WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(),
                    false);
        }
        if (openCoalBag != null) {
            return createMenuEntry(9,
                    MenuAction.CC_OP_LOW_PRIORITY,
                    openCoalBag.getIndex(),
                    WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(),
                    false);
        }
        return null;
    }
    private void walkTile(WorldPoint worldpoint) {
        int x = worldpoint.getX() - client.getBaseX();
        int y = worldpoint.getY() - client.getBaseY();
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    //BOOLEANS
    private boolean shouldResetTimeout() {
        if (timeout > 40) {
            return false;
        }
        switch (action) {
            case "openBank":
                return isbankOpen();
            case "depositOre":
                if (oreInInvent()) {
                    waitBars = 1;
                }
                if (getEmptySlots() > 25) {
                    return true;
                }
            case "depositProds":
                return getEmptySlots() >25;
            case "emptyBag":
                return getInventoryItem(ItemID.COAL) != null;
            case "walktobarstile":
                return isatBarsTile() && xpDrop;
            case "withdrawBars":
                return isatBarsTile() && dispenserState > 1;
            case "withdrawOre":
            case "withdrawCoal":
                return getInventoryItem(config.barType().getBarID())  == null
                        && getEmptySlots() < 1;
            case "takeBars":
                if (getInventoryItem(config.barType().getBarID()) != null) {
                    waitBars = 0;
                    return true;
                }

        }
        return false;
    }
    private boolean isCofferMoney() {
        final int coffer = client.getVarbitValue(BLAST_FURNACE_COFFER);
        if (coffer < 10) {
            return false;
        }
        return true;
    }
    private boolean oreInInvent() {
        int ore = config.barType().getOreID();
        return (getInventoryItem(ore)!= null);
    }
    private boolean coalInInvent() {return getInventoryItem(453) != null;}
    private boolean isBesideBelt() {
        return (getGameObject(9100).getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation())<2);
    }
    private boolean isatBarsTile() {
        WorldPoint tile1 = new WorldPoint(1940,4962,0);
        WorldPoint tile3 = new WorldPoint(1940,4964,0);
        return (client.getLocalPlayer().getWorldLocation().distanceTo(tile1) < 2
                || client.getLocalPlayer().getWorldLocation().distanceTo(tile3) < 2);
    }
    private boolean shouldConsume() {
        return (client.getLocalPlayer().getAnimation() == client.getLocalPlayer().getRunAnimation()
                || timeout > 0
                || client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null);
    }
    private boolean isbankOpen() {return client.getItemContainer(InventoryID.BANK) != null;}
    private boolean checkifStuck() {return (lastaction == action);}
    private boolean outofMaterials(int id) {
        return (getBankIndex(id) == -1
                && isbankOpen());
    }
    private boolean hasGoldGloves() {
        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipmentContainer == null) {
            return false;
        }
        return (getInventoryItem(ItemID.GOLDSMITH_GAUNTLETS) != null);
        //return (equipmentContainer.contains(ItemID.GOLDSMITH_GAUNTLETS)
        //        || getInventoryItem(ItemID.GOLDSMITH_GAUNTLETS) != null);
    }
    private boolean hasIceGloves() {
        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        ItemContainer invyContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (equipmentContainer == null
                || invyContainer == null) {
            return false;
        }
        return (invyContainer.contains(ItemID.ICE_GLOVES));
        //return (equipmentContainer.contains(ItemID.ICE_GLOVES)
        //        || invyContainer.contains(ItemID.ICE_GLOVES));
    }

    //EXTRAS
    private void walktoBarPickup() {
        WorldPoint worldpoint = new WorldPoint(1940,4962,0);
        walkTile(worldpoint);
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
    private void debug(int timeOut) {
        System.out.println("action=" + action + " timeout=" + timeOut
                + " stuckCounter=" + stuckCounter
                + " waitBars=" + waitBars);
    }
    private Point getLocation(TileObject tileObject) {
        if (tileObject instanceof GameObject)
            return ((GameObject) tileObject).getSceneMinLocation();
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }
    private GameObject getGameObject(int id) {
        return new GameObjectQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    private Widget getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (inventoryWidget != null && !inventoryWidget.isHidden())
        {
            return getWidgetItem(inventoryWidget,id);
        }
        if (bankInventoryWidget != null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget,id);
        }
        return null;
    }
    private Widget getWidgetItem(Widget widget,int id) {
        for (Widget item : widget.getDynamicChildren())
        {
            if (item.getItemId() == id)
            {
                return item;
            }
        }
        return null;
    }
    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}