package net.runelite.client.plugins.a1cbankskills;
import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
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
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.plugins.woodcutting.config.ClueNestTier;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "A1C Bank Skillz",
        enabledByDefault = false,
        description = "OP Adam Bank Skillz")
@Slf4j
public class A1CBankSkillsPlugin extends Plugin
{
    @Inject
    private Client client;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private A1CBankSkillsConfig config;
    @Provides
    A1CBankSkillsConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(A1CBankSkillsConfig.class);
    }
    private String skillStage;
    private int timeout;
    private int isFirstDeposit;
    private int idprod;
    private int id1;
    private int id2;
    private int id3;
    private int craftNum;
    private long withdrawextra;
    private long withdrawextratmp;
    private int stuckCounter = 0;
    private String lastaction;
    private String BANKTYPE;
    private int BANKid;
    private int pickupStatus;
    private Types.Skill skillOpt;
    private ItemSpawned groundItem;

    @Override
    protected void startUp() {
        timeout = 0;
        skillStage = null;
        isFirstDeposit = 1;
        idprod = 0;
        id1 = 0;
        id2 = 0;
        craftNum = 0;
        stuckCounter = 0;
        updateConfig();
    }
    @Override
    protected void shutDown() {
        updateConfig();
        timeout = 0;
        skillStage = null;
        isFirstDeposit = 1;
        idprod = 0;
        id1 = 0;
        id2 = 0;
        craftNum = 0;
    }
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        updateConfig();
    }
    @Subscribe
    public void onItemSpawned(ItemSpawned itemSpawned)
    {
            groundItem = itemSpawned;
    }
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (event.getMenuOption().equals("<col=00ff00>One Click Adam Bank Skillz"))
        {
            if (isbankOpen()) {
                if (client.getVarbitValue(6590) != 0) {
                    event.setMenuEntry(createMenuEntry(1, MenuAction.CC_OP, -1, 786460, false));
                    skillStage = "setwithdrawOpt";
                    return;
                }
                if (client.getVarbitValue(Varbits.CURRENT_BANK_TAB) != 0) {
                    event.setMenuEntry(createMenuEntry(1, MenuAction.CC_OP, 10, WidgetInfo.BANK_TAB_CONTAINER.getId(), false));
                    skillStage = "setbanktab";
                    return;
                }
            } //set bank options
            if (shouldConsume()) {
                System.out.println("Consumed. Timeout = " + timeout);
                event.consume();
                return;
            }
            if (stuckCounter > 10) {
                sendGameMessage("Stuck on step " + skillStage + ". Logging out in 15 seconds.");
                timeout = 75;
                return;
            }
            int lasttimeout = timeout;
            if (skillOpt == Types.Skill.CastSpell) {
                lastaction = skillStage;
                spellclickHandler(event);
                debug(lasttimeout);
            } else {
                lastaction = skillStage;
                clickHandler(event);
                debug(lasttimeout);
            }
            if (checkifStuck() && !(skillStage == "pickupGlass")) {
                stuckCounter = stuckCounter +1;
                return;
            }
            stuckCounter = 0;
        }
    }
    @Subscribe
    public void onGameTick(GameTick event) {
        if (idprod == 0 || id1 == 0 || id2 == 0) {
            updateConfig();
        }
        if (timeout > 0) {
            timeout--;
        }
        if (timeout == 50) {
            timeout = 0;
            logout();
            return;
        }
        if (client.getLocalPlayer().getAnimation() != -1
                && !(client.getLocalPlayer().getAnimation() == 6294
                || client.getLocalPlayer().getAnimation() == 4413)) {
            timeout = 4;
        }
        if ((countInvIDs(idprod) == 27
                && skillOpt != Types.Skill.CastSpell)
                || (countInvIDs(idprod) == 14
                && getInventoryItem(id2) == null)) {
            timeout = 0;
        }
    }
    @Subscribe
    private void onClientTick(ClientTick event) {
        if (client.getLocalPlayer() == null
                || client.getGameState() != GameState.LOGGED_IN
                || getGameObject(BANKid) == null
                || client.getWidget(378, 78) != null) //login button
        {
            return;
        }

        String text = "<col=00ff00>One Click Adam Bank Skillz";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x -> x.getOption().equals(text)).findFirst().orElse(null));
    }

    //Handles clicks
    private void clickHandler(MenuOptionClicked event) {
        if (isCraftingMenuOpen()) {
            event.setMenuEntry(selectCraftOption());
            timeout = 3;
            skillStage = "pushcraftopt";
            return;
        }
        if (useItemOnItem() != null) {
            event.setMenuEntry(useItemOnItem());
            timeout = 1;
            skillStage = "useitemonitem";
            return;
        }
        if (!isbankOpen()) {
            event.setMenuEntry(openBank());
            timeout = 1;
            skillStage = "openbank";
            return;
        }
        if (shouldDeposit()) {
            if (shouldDepositProducts()) {
                event.setMenuEntry(depositAllProducts());
                timeout = 1;
                skillStage = "depositprods";
                return;
            }
            event.setMenuEntry(depositItems());
            isFirstDeposit = 0;
            skillStage = "depositall";
            return;
        }
        if (getInventoryItem(id1) == null) {
            event.setMenuEntry(withdrawItem(id1, skillOpt));
            timeout = 1;
            skillStage = "withdrawid1";
            return;
        }
        if (getInventoryItem(id2) == null) {
            event.setMenuEntry(withdrawItem(id2, skillOpt));
            timeout = 1;
            skillStage = "withdrawid2";
            return;
        }
        skillStage = "idle";
    }

    //Handles spell clicks
    private void spellclickHandler(MenuOptionClicked event) {
        if (shouldPickUpGlass()) {
            event.setMenuEntry(pickUpGlass());
            timeout = 1;
            skillStage = "pickupGlass";
            return;
        }
        if (shouldCastSpell()) {
            event.setMenuEntry(castspell());
            withdrawextratmp = withdrawextra;
            timeout = 5;
            skillStage = "castspell";
            return;
        }
        if (!isbankOpen()) {
            event.setMenuEntry(openBank());
            timeout = 1;
            skillStage = "openbank";
            return;
        }
        if (shouldDeposit()) {
            if (shouldDepositProducts()) {
                event.setMenuEntry(depositAllProducts());
                timeout = 1;
                isFirstDeposit = 0;
                skillStage = "depositprods";
                return;
            }
            event.setMenuEntry(depositItems());
            timeout = 1;
            skillStage = "depositall";
            return;
        }
        if (getInventoryItem(id3) == null) {
            event.setMenuEntry(withdrawItem(id3, skillOpt));
            timeout = 0;
            skillStage = "withdrawid3";
            return;
        }
        if (id1 != -1 && getInventoryItem(id1) == null
                || withdrawextratmp > 0) {
            event.setMenuEntry(withdrawItem(id1, skillOpt));
            timeout = 0;
            withdrawextratmp = withdrawextratmp - 1;
            skillStage = "withdrawid1";
            if (withdrawextratmp == 0) {
                timeout = 1;
            }
            return;
        }

        withdrawextratmp = withdrawextra - countInvIDs(id1);
        if (getInventoryItem(id2) == null) {
            event.setMenuEntry(withdrawItem(id2, skillOpt));
            timeout = 2;
            skillStage = "withdrawid2";
            return;
        }
        skillStage = "idle";
    }

    //SUBROUTINES
    private void updateConfig() {
        updateBankinfo();
        if (config.skill() == Types.Skill.Custom) {
            updateCustominfo();
            return;
        }
        isFirstDeposit = 1;
        stuckCounter = 0;
        skillOpt = config.skill();
        withdrawextratmp = 0;
        withdrawextra = 0;
        id3 = -1;
        if (config.skill() == Types.Skill.Use14on14) {
            craftNum = config.product14on14().craftOpt;
            idprod = config.product14on14().productid;
            id1 = config.product14on14().ingredientid1;
            id2 = config.product14on14().ingredientid2;
            return;
        }
        if (config.skill() == Types.Skill.Use1on27) {
            craftNum = config.product1on27().craftOpt;
            idprod = config.product1on27().productid;
            id1 = config.product1on27().ingredientid1;
            id2 = config.product1on27().ingredientid2;
            return;
        } else {
            craftNum = config.productcastspell().craftOpt;
            idprod = config.productcastspell().productid;
            id1 = config.productcastspell().ingredientid1;
            id2 = config.productcastspell().ingredientid2;
            id3 = 12791;
            if (config.productcastspell() == Types.Productcastspell.SUPERGLASSMAKE)
            {
                pickupStatus = 0;
                withdrawextratmp =0;
                withdrawextra = 3;
                withdrawextratmp = withdrawextra - countInvIDs(id1);
            }
        }
    }
    private void updateBankinfo() {
        if (config.bank() == Types.Banks.Custom) {
            BANKTYPE = config.banktype().Type;
            BANKid = config.bankid();
        }
        BANKTYPE = config.bank().Type;
        BANKid = config.bank().ID;
    }
    private void updateCustominfo() {
        withdrawextra = 0;
        withdrawextratmp = 0;
        stuckCounter = 0;
        isFirstDeposit = 1;
        craftNum = config.craftNum();
        skillOpt = config.customskill();
        idprod = config.customproductID();
        id1 = config.customingredientID1();
        id2 = config.customingredientID2();
        id3 = -1;
    }
    private long countInvIDs(Integer id) {
        if (isbankOpen()) {
            List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getChildren());
            return (inventoryWidget.stream().filter(item -> item.getItemId() == id).count());
        }
        List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getChildren());
        return (inventoryWidget.stream().filter(item -> item.getItemId() == id).count());
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

    //ACTIONS
    private MenuEntry openBank() {
        if (BANKTYPE == Types.BankType.BOOTH.Type)
        {
            GameObject gameObject = getGameObject(BANKid);
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_SECOND_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    false);
        }

        if (BANKTYPE == Types.BankType.CHEST.Type)
        {
            GameObject gameObject = getGameObject(BANKid);
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    false);
        }

        if (BANKTYPE == Types.BankType.NPC.Type)
        {
            NPC npc = getNpc(BANKid);
            return createMenuEntry(
                    npc.getIndex(),
                    MenuAction.NPC_THIRD_OPTION,
                    getNPCLocation(npc).getX(),
                    getNPCLocation(npc).getY(),
                    false);
        }
        return null;
    }
    private MenuEntry depositItems() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                786474,
                false);
    }
    private MenuEntry depositAllProducts() {
        Widget item1 = getInventoryItem(idprod);
        if (item1 == null)
        {
            return null;
        }
        return createMenuEntry(
                8,
                MenuAction.CC_OP_LOW_PRIORITY,
                item1.getIndex(),
                983043,
                false);
    }
    private MenuEntry withdrawItem(Integer configID, Enum type) {
        int amtID =0;
        if (getBankIndex(configID) == -1) return null;
        if (type == Types.Skill.Use1on27
                || type == Types.Skill.CastSpell)
        {
            if (configID == id1)
            {
                amtID = 1;  //withdraw 1
            } else {
                amtID = 7; //withdraw all
            }
        }
        if (type == Types.Skill.Use14on14
                || (type == Types.Skill.CastSpell
                && config.productcastspell() == Types.Productcastspell.SUPERGLASSMAKE
                && configID == id2))
        {
            amtID = 5; //withdraw 14
        }
        if (amtID == 0)
        {
            return null;
        }
        return createMenuEntry(
                amtID,
                MenuAction.CC_OP,
                getBankIndex(configID),
                786445,
                false);
    }
    private MenuEntry useItemOnItem() {
        Widget item1 = getInventoryItem(id1);
        Widget item2 = getInventoryItem(id2);
        if (item1 == null || item2 == null) {
            return null;
        }
        setSelectedInventoryItem(item1);
        return createMenuEntry(0,
                MenuAction.WIDGET_TARGET_ON_WIDGET,
                item2.getIndex(),
                9764864,
                false);
    }
    private MenuEntry selectCraftOption() {
        int menuID = 17694734 + craftNum - 1;
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                menuID,
                false);
    }
    private MenuEntry castspell() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                config.productcastspell().spellname.getId(),
                false);
    }
    private MenuEntry pickUpGlass() {
        TileItem item = groundItem.getItem();
        Tile tile = groundItem.getTile();
        if (item == null)
        {
            return null;
        }
        return client
                .createMenuEntry(3)
                .setOption("Take")
                .setTarget("Molten glass")
                .setIdentifier(groundItem.getItem().getId())
                .setType(MenuAction.GROUND_ITEM_THIRD_OPTION)
                .setParam0(tile.getSceneLocation().getX())
                .setParam1(tile.getSceneLocation().getX())
                .setForceLeftClick(false);
    }

    //BOOLEANS
    private boolean isInvEmpty() {
        if (isbankOpen()) {
            List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return inventoryWidget.stream().allMatch(item -> item.getItemId() == 6512);
        }
        List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
        return inventoryWidget.stream().allMatch(item -> item.getItemId() == 6512);
    }
    private boolean isInvFull() {
        if (isbankOpen()) {
            List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (inventoryWidget.stream().noneMatch(item -> item.getItemId() == 6512));
        }
        List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
        return (inventoryWidget.stream().noneMatch(item -> item.getItemId() == 6512));
    }
    private boolean isbankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }
    private boolean isCraftingMenuOpen() {
        return client.getWidget(WidgetInfo.MULTI_SKILL_MENU) != null
                && skillStage == "useitemonitem";
    }
    private boolean shouldDeposit() {
        long cEmpty = countInvIDs(6512);
        long cProd = countInvIDs(idprod);
        long cId1 = countInvIDs(id1);
        long cId2 = countInvIDs(id2);
        if (skillOpt == Types.Skill.CastSpell
                && config.productcastspell() == Types.Productcastspell.SUPERGLASSMAKE)
        {
            return (((getInventoryItem(id1) == null
                    && getInventoryItem(id2) == null
                    && getInventoryItem(id3) == null)
                    && !isInvEmpty())
                    || getInventoryItem(idprod) != null
                    || ((getInventoryItem(id1) == null
                    || getInventoryItem(id2) == null
                    || getInventoryItem(id3) == null)
                    && isInvFull())
                    || cEmpty + cProd + cId1 + cId2 + countInvIDs(id3) != 28
                    || shouldDepositProducts());
        }
            return (((getInventoryItem(id1) == null
                    && getInventoryItem(id2) == null)
                    && !isInvEmpty())
                    || getInventoryItem(idprod) != null
                    || ((getInventoryItem(id1) == null
                    || getInventoryItem(id2) == null)
                    && isInvFull())
                    || cEmpty + cProd + cId1 + cId2 != 28
                    || shouldDepositProducts());
    }
    private boolean shouldDepositProducts() {
        long cEmpty = countInvIDs(6512);
        long cProd = countInvIDs(idprod);
        long cId1 = countInvIDs(id1);
        long cId2 = countInvIDs(id2);
        if (skillOpt == Types.Skill.Use14on14) {
            return (isFirstDeposit != 1 && (getInventoryItem(idprod) != null
                    && getInventoryItem(id1) == null
                    && getInventoryItem(id2) == null
                    && cEmpty + cProd + cId1 + cId2 == 28));
        }
        if (skillOpt == Types.Skill.Use1on27) {
            return (isFirstDeposit != 1 && (getInventoryItem(idprod) != null
                    && getInventoryItem(id2) == null
                    && cEmpty + cProd + cId1 + cId2 == 28));
        }
        if (skillOpt == Types.Skill.CastSpell) {
            return (isFirstDeposit != 1 && (getInventoryItem(idprod) != null
                    && getInventoryItem(id2) == null
                    && cEmpty + cProd + cId1 + cId2 + countInvIDs(id3) == 28));
        }
        return false;
    }
    private boolean outofMaterials() {
        return (((getBankIndex(id1) == -1
                && getInventoryItem(id1) == null)
                || (getBankIndex(id2) == -1
                && getInventoryItem(id2) == null))
                && isbankOpen());
    }
    private boolean shouldCastSpell() {
        if (skillOpt == Types.Skill.CastSpell
                && config.productcastspell() == Types.Productcastspell.SUPERGLASSMAKE)
        {
            return skillOpt == Types.Skill.CastSpell
                    && countInvIDs(id1) >= 3
                    && countInvIDs(id2) >= 18
                    && countInvIDs(id3) >= 1
                    && (3 - countInvIDs(id1)) <= 0;
        }
        return skillOpt == Types.Skill.CastSpell
                && getInventoryItem(id1) != null
                && getInventoryItem(id2) != null;
    }
    private boolean shouldConsume() {
    if (!config.consumeMisclicks())
    {
        return false;
    }
    return ((client.getLocalPlayer().getAnimation() != -1
            && !(client.getLocalPlayer().getAnimation() == 6294
            || client.getLocalPlayer().getAnimation() == 4413))
//            || client.getLocalPlayer().getAnimation() == 363
//            || client.getLocalPlayer().getAnimation() == 1248
//            || client.getLocalPlayer().getAnimation() == 884
//            || client.getLocalPlayer().getAnimation() == 6688
//            || client.getLocalPlayer().getAnimation() == 6689))
            || getGameObject(BANKid) == null
            || timeout > 0
            || outofMaterials()
            || client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null);
}
    private boolean shouldPickUpGlass() {
        if (groundItem.getItem().getId() == idprod
                && !isInvFull())
        {
            if (groundItem.getTile().getGroundItems() != null
                    && groundItem.getTile().getGroundItems().stream()
                    .filter(tileItem -> tileItem.getId() == idprod).count() >= config.pickupSP()
                    || pickupStatus == 1)
            {
                pickupStatus = 1;
                if (groundItem.getTile().getGroundItems() == null
                        || groundItem.getTile().getGroundItems().stream()
                        .filter(tileItem -> tileItem.getId() == idprod).count() == 0)
                {
                    pickupStatus = 0;
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    private boolean checkifStuck() {
        return (lastaction == skillStage);
    }

    //EXTRAS
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
        System.out.println("skillstage=" + skillStage + " timeout=" + timeOut
                + " stuckCounter=" + stuckCounter + " withdrawextratmp=" + withdrawextratmp
                + " shouldconsume=" + shouldConsume());
    }
    private int getBankIndex(int id) {
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(id)
                .result(client)
                .first();
        if (bankItem == null) return -1;
        return bankItem.getWidget().getIndex();
    }
    private void setSelectedInventoryItem(Widget item) {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getItemId());
    }
    private Point getLocation(TileObject tileObject) {
        if (tileObject == null)
        {
            return new Point(0, 0);
        }
        if (tileObject instanceof GameObject)
        {
            return ((GameObject) tileObject).getSceneMinLocation();
        }
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
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
    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}