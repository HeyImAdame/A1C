package net.runelite.client.plugins.a1cconstruction;

import javax.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.rs.api.RSClient;
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
        name = "A1C Construction",
        description = "Have coins/items/butler setup",
        tags = {"one", "click", "con", "construction", "adam", "construction"},
        enabledByDefault = false
)
public class A1CConstructionPlugin extends Plugin
{
    @Inject
    private Client client;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private A1CConstructionConfig config;
    @Provides
    A1CConstructionConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(A1CConstructionConfig.class);
    }
    private int timeout;
    private String action;
    private int forcelogout;
    private int butlerplanks;
    private int emptyslots;
    private int stuckCounter;
    private String lastaction;
    private boolean unnoting;
    private int buildstep;
    private WorldPoint startLoc;

    @Override
    protected void startUp() throws Exception {
        timeout = 0;
        action = "";
        forcelogout = 0;
        stuckCounter = 0;
        butlerplanks = 26;
        emptyslots = 0;
        unnoting = false;
        buildstep = 1;
        startLoc = new WorldPoint(client.getLocalPlayer().getWorldLocation().getX()
                ,client.getLocalPlayer().getWorldLocation().getY()
                ,client.getLocalPlayer().getWorldLocation().getPlane());
    }
    @Override
    protected void shutDown() throws Exception {
        timeout = 0;
        action = "";
        forcelogout = 0;
        stuckCounter = 0;
        butlerplanks = 0;
        emptyslots = 0;
        unnoting = false;
    }
    @Subscribe
    private void onNpcSpawned(final NpcSpawned event) {
        final NPC npc = event.getNpc();

        if (npc.getId() == 229)
        {
            unnoting = false;
        }
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getItemContainer() == client.getItemContainer(InventoryID.INVENTORY))
        {
            if (emptyslots > getEmptySlots()) {
                butlerplanks = butlerplanks - (emptyslots - getEmptySlots());
                emptyslots = getEmptySlots();
                return;
            }
            emptyslots = getEmptySlots();
        }
    }
    @Subscribe
    public void OnGameTick(GameTick event) {
        if (shouldResetTimeout()) {
            timeout = 0;
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
        if (!isInPOH()) {return;}
        String text;
            text =  "<col=00ff00>Mexican Adam";

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
        if (event.getMenuOption().equals("<col=00ff00>Mexican Adam")) {
            if (shouldConsume()) {
                System.out.println("Consumed. Timeout = " + timeout);
                event.consume();
                return;
            }
            if (!hasItems()) {
                sendGameMessage("Missing items. Need hammer, saw, coins, and noted planks");
                event.consume();
                return;
            }
            if (stuckCounter > 10) {
                sendGameMessage("No more materials found. Logging out in 15 seconds.");
                timeout = 75;
                return;
            }
                lastaction = action;
            if (config.build() == Types.Build.MAHOGANY_TABLE) {
                handleMahoganyClick(event);
                debug();
            }
            if (config.build() == Types.Build.TEAK_BENCH) {
                handleTeakClick(event);
                debug();
            }

            if (checkifStuck()) {
                stuckCounter = stuckCounter + 1;
                return;
            }
            stuckCounter = 0;
        }
    }

    //Handle clicks
    private void handleMahoganyClick(MenuOptionClicked event)
    {
        if (!isAtStartLoc()) {
            walkTile(startLoc);
            action = "walkTile";
            timeout = 4;
        }
        if (shouldClickContinue()) {
            if (client.getWidget(219, 1) != null
                    && client.getWidget(219, 1).getChild(1).getText().contains("Un-note:")) {
                event.setMenuEntry(clickContinueMES());
                action = "unnote";
                unnoting = true;
                butlerplanks = 26;
                timeout = 0;
                return;
            }
            if (client.getWidget(231, 6) != null
                    && client.getWidget(231, 6).getText().contains("I have returned with")) {
                //do nothing
            } else {
                event.setMenuEntry(clickContinueMES());
                action = "clickcontinue";
                timeout = 0;
                return;
            }
        }

        if (client.getWidget(458, 9) != null
                && !client.getWidget(458, 9).isHidden()) {
            event.setMenuEntry(buildSelect());
            action = "buildthing";
            timeout = 2;
            return;
        } //check if build menu opened

        if (shouldGetButlerPlanks() || client.getWidget(370, 19) != null) {
            if (!isButler() && !unnoting) {
                if (client.getWidget(370, 19) != null
                        && client.getWidget(370, 19).getChild(3) != null) {
                    event.setMenuEntry(callButlerMES());
                    timeout = 1;
                    action = "callButler";
                    return;
                }
                if (client.getWidget(116, 8) != null) {
                    event.setMenuEntry(houseOptionsMES());
                    timeout = 1;
                    action = "houseOpts";
                    return;
                }
            }
        event.setMenuEntry(clickButler());
        action = "clickbutler";
        timeout = 1;
        return;
        }

        if (getGameObject(config.build().builtID) != null) {
            event.setMenuEntry(removeMenu(getGameObject(config.build().builtID)));
            action = "clickremove";
            timeout = 1;
            return;
        }

        if (countInvIDs(config.build().plankID) > 5) {
            event.setMenuEntry(buildMenu(getGameObject(config.build().unbuiltID)));
            action = "clickbuild";
            timeout = 1;
            return;
        }
        action = "idle";
    }

    private void handleTeakClick(MenuOptionClicked event)
    {
        if (!isAtStartLoc()) {
            walkTile(startLoc);
            action = "walkTile";
            timeout = 4;
        }
        if (shouldClickContinue()) {
            if ((client.getWidget(219, 1) != null
                    && client.getWidget(219, 1).getChild(1).getText().contains("Un-note:"))
                    || (client.getWidget(231, 1) != null)
                    && client.getWidget(231, 1).getText().contains("I can only carry")) {
                event.setMenuEntry(clickContinueMES());
                action = "unnote";
                unnoting = true;
                butlerplanks = 26;
                timeout = 0;
                return;
            }
            if (client.getWidget(231, 6) != null
                    && client.getWidget(231, 6).getText().contains("I have returned with")) {
                //do nothing
            } else {
                event.setMenuEntry(clickContinueMES());
                action = "clickcontinue";
                timeout = 0;
                return;
            }
        }

        if (client.getWidget(458, 4) != null
                && !client.getWidget(458, 4).isHidden()) {
            event.setMenuEntry(buildSelect());
            action = "buildthing";
            timeout = 2;
            return;
        } //check if build menu opened

        if (shouldGetButlerPlanks() || client.getWidget(370, 19) != null) {
            if (!isButler() && !unnoting) {
                if (client.getWidget(370, 19) != null
                        && client.getWidget(370, 19).getChild(3) != null) {
                    event.setMenuEntry(callButlerMES());
                    timeout = 1;
                    action = "callButler";
                    return;
                }
                if (client.getWidget(116, 8) != null) {
                    event.setMenuEntry(houseOptionsMES());
                    timeout = 1;
                    action = "houseOpts";
                    return;
                }
            }
            event.setMenuEntry(clickButler());
            action = "clickbutler";
            timeout = 0;
            return;
        }

        if (countInvIDs(config.build().plankID) > 5) {
            GameObject G1;
            GameObject G2;
            GameObject G3;
            GameObject G4;
            if (buildstep == 2) {
                G1 = getObjectinRange(config.build().unbuiltID, 0, 2);
                G2 = getObjectinRange(config.build().builtID, 0, 2);
                G3 = getObjectinRange(config.build().unbuiltID2, 0, 2);
                G4 = getObjectinRange(config.build().builtID2, 0, 2);
                buildstep = buildstep - 1;
                if (G1 != null) {
                    action = "build1";
                    event.setMenuEntry(buildMenu(G1));
                    timeout = 1;
                    return;
                }
                if (G2 != null) {
                    action = "remove1";
                    event.setMenuEntry(removeMenu(G2));
                    timeout = 1;
                    return;
                }
                if (G3 != null) {
                    action = "build2";
                    event.setMenuEntry(buildMenu(G3));
                    timeout = 1;
                    return;
                }
                if (G4 != null) {
                    action = "remove2";
                    event.setMenuEntry(removeMenu(G4));
                    timeout = 1;
                    return;
                }
            }
            if (buildstep == 1) {
                buildstep = buildstep + 1;
                G1 = getObjectinRange(config.build().unbuiltID, 2, 0);
                G2 = getObjectinRange(config.build().builtID, 2, 0);
                G3 = getObjectinRange(config.build().unbuiltID2, 2, 0);
                G4 = getObjectinRange(config.build().builtID2, 2, 0);
                if (G1 != null) {
                    action = "build1";
                    event.setMenuEntry(buildMenu(G1));
                    timeout = 1;
                    return;
                }
                if (G2 != null) {
                    action = "remove1";
                    event.setMenuEntry(removeMenu(G2));
                    timeout = 1;
                    return;
                }
                if (G3 != null) {
                    action = "build2";
                    event.setMenuEntry(buildMenu(G3));
                    timeout = 1;
                    return;
                }
                if (G4 != null) {
                    action = "remove2";
                    event.setMenuEntry(removeMenu(G4));
                    timeout = 1;
                    return;
                }

            }
        }
        action = "idle";
    }

    //SUBROUTINES
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
    private MenuEntry removeMenu(GameObject G) {
        if (config.build() == Types.Build.TEAK_BENCH) {
            if (action == "remove1") {
                //GameObject G = getObjectinRange(config.build().builtID);
                if (G == null) {
                    return null;
                }
                //buildstep = 4;
                return client
                        .createMenuEntry(0)
                        .setOption("Remove")
                        .setTarget("Teak bench")
                        .setIdentifier(config.build().builtID)
                        .setType(MenuAction.GAME_OBJECT_FIFTH_OPTION)
                        .setParam0(getLocation(G).getX())
                        .setParam1(getLocation(G).getY())
                        .setForceLeftClick(false);
            }
            else if (action == "remove2") {
                //GameObject G = getObjectinRange(config.build().builtID2);
                if (G == null) {
                    return null;
                }
                //buildstep = 1;
                return client
                    .createMenuEntry(0)
                    .setOption("Remove")
                    .setTarget("Teak bench")
                    .setIdentifier(config.build().builtID2)
                    .setType(MenuAction.GAME_OBJECT_FIFTH_OPTION)
                    .setParam0(getLocation(G).getX())
                    .setParam1(getLocation(G).getY())
                    .setForceLeftClick(false);
            }
        }
        if (config.build() == Types.Build.MAHOGANY_TABLE) {
        return client
                .createMenuEntry(0)
                .setOption("Remove")
                .setTarget("Mahogany table")
                .setIdentifier(config.build().builtID)
                .setType(MenuAction.GAME_OBJECT_FIFTH_OPTION)
                .setParam0(getLocation(getGameObject(config.build().builtID)).getX())
                .setParam1(getLocation(getGameObject(config.build().builtID)).getY())
                .setForceLeftClick(false);
        }
        return null;
    }
    private MenuEntry buildMenu(GameObject G) {
        if (config.build() == Types.Build.TEAK_BENCH) {
            if (action == "build1") {
                //GameObject G = getObjectinRange(config.build().unbuiltID);
                if (G == null) {
                    return null;
                }
                //buildstep = 2;
                return client.createMenuEntry(0)
                        .setOption("Build")
                        .setTarget("Seating space")
                        .setIdentifier(config.build().unbuiltID)
                        .setType(MenuAction.GAME_OBJECT_FIFTH_OPTION)
                        .setParam0(getLocation(G).getX())
                        .setParam1(getLocation(G).getY())
                        .setForceLeftClick(false);
            }
            else if (action == "build2"){
                //GameObject G = getObjectinRange(config.build().unbuiltID2);
                if (G == null) {
                    return null;
                }
                //buildstep = 3;
                return client.createMenuEntry(0)
                        .setOption("Build")
                        .setTarget("Seating space")
                        .setIdentifier(config.build().unbuiltID2)
                        .setType(MenuAction.GAME_OBJECT_FIFTH_OPTION)
                        .setParam0(getLocation(G).getX())
                        .setParam1(getLocation(G).getY())
                        .setForceLeftClick(false);
            }
        }
        if (config.build() == Types.Build.MAHOGANY_TABLE) {
        return client
                .createMenuEntry(0)
                .setOption("Build")
                .setTarget("Table space")
                .setIdentifier(ObjectID.TABLE_SPACE)
                .setType(MenuAction.GAME_OBJECT_FIFTH_OPTION)
                .setParam0(getLocation(getGameObject(config.build().unbuiltID)).getX())
                .setParam1(getLocation(getGameObject(config.build().unbuiltID)).getY())
                .setForceLeftClick(false);
        }
        return null;
}
    private MenuEntry buildSelect() {
        if (config.build() == Types.Build.TEAK_BENCH) {
            return client.createMenuEntry(0)
                    .setOption("Build")
                    .setTarget("Teak garden bench")
                    .setIdentifier(1)
                    .setType(MenuAction.CC_OP)
                    .setParam0(-1)
                    .setParam1(30015492)
                    .setForceLeftClick(false);
        }
        if (config.build() == Types.Build.MAHOGANY_TABLE) {
            return client.createMenuEntry(0)
                    .setOption("Build")
                    .setTarget("Mahogany table")
                    .setIdentifier(1)
                    .setType(MenuAction.CC_OP)
                    .setParam0(-1)
                    .setParam1(30015497)
                    .setForceLeftClick(false);
        }
        return null;
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
        if (client.getWidget(WidgetInfo.DIALOG_OPTION_OPTION1) != null) {
            if (!client.getWidget(WidgetInfo.DIALOG_OPTION_OPTION1).getChild(3).getText().contains("sawmill")) {
                return createMenuEntry(0,
                        MenuAction.WIDGET_CONTINUE,
                        1,
                        WidgetInfo.DIALOG_OPTION_OPTION1.getId(),
                        false);
            }
        }
        return client
                .createMenuEntry(1)
                .setOption("Continue")
                .setTarget("")
                .setIdentifier(0)
                .setType(MenuAction.WIDGET_CONTINUE)
                .setParam0(-1)
                .setParam1(15138821)
                .setForceLeftClick(false);
    }
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
    private boolean isAtStartLoc() {
        return (client.getLocalPlayer().getWorldLocation().distanceTo(startLoc) == 0);
    }
    private boolean shouldGetButlerPlanks() {
        return (countInvIDs(config.build().plankID) < 13
                && getEmptySlots() > 0
                && unnoting == false
                || (butlerplanks <= getEmptySlots())
                || butlerplanks < 1
                || getInventoryItem(config.build().plankID) == null);
    }
    private boolean shouldResetTimeout() {
        if (timeout > 40) {
            return false;
        }
        switch (action) {
            case "buildthing":
                return getGameObject(config.build().builtID) != null;
            case "clickremove":
                return getGameObject(config.build().builtID) == null;
            case "clickbuild":
                return client.getWidget(458, 9) != null
                        && !client.getWidget(458, 9).isHidden();
        }
        return false;
    }
    private boolean isInPOH() {
        return getGameObject(4525) != null; //checks for portal, p sure this is same for everyone if not need to do alternative check.
    }
    private boolean isButler() {
        NPC npc = getNpc(229);
        if (npc == null) {
            return false;
        }
        return (npc.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 2);
    }
    private boolean shouldClickContinue() {
        if (client.getWidget(231, 5) == null) {
            if (client.getWidget(219, 1) != null) {
                return !client.getWidget(219, 1).isHidden();
            }
                return false;
        }
        if (client.getWidget(219, 1) == null) {
            if (client.getWidget(231, 5) != null) {
                return (!client.getWidget(231, 5).isHidden()
                        && getEmptySlots() != 0);
            }
                return false;
        }
        return !client.getWidget(231, 5).isHidden()
                || !client.getWidget(219, 1).isHidden();
    }
    private boolean hasItems() {
        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if ((getInventoryItem(config.build().plankID) != null
                || getInventoryItem(config.build().plankID + 1) != null)
                && getInventoryItem(ItemID.SAW) != null
                && getInventoryItem(ItemID.COINS_995) != null) {
            int coinInd = getInventoryItem(ItemID.COINS_995).getIndex();
            int coinAmt = client.getWidget(WidgetInfo.INVENTORY).getChild(coinInd).getItemQuantity();
            int plankInd = getInventoryItem(config.build().plankID + 1).getIndex();
            int plankAmt = client.getWidget(WidgetInfo.INVENTORY).getChild(plankInd).getItemQuantity();
            if (coinAmt > 10000 && plankAmt > 5) {
                return true;
            }
        }
        return false;
    }
    private boolean checkifStuck() {
        return (lastaction == action);
    }
    private boolean shouldConsume() {
        return (timeout > 0
                || client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null);
    }

    //EXTRAS
    private GameObject getObjectinRange(int id, int idx, int idy) {
        WorldArea area = new WorldArea(new WorldPoint(client.getLocalPlayer().getWorldLocation().getX() - idx,client.getLocalPlayer().getWorldLocation().getY() - idy,0)
                ,new WorldPoint(client.getLocalPlayer().getWorldLocation().getX() + idx,client.getLocalPlayer().getWorldLocation().getY() + idy,0));
        GameObject obj = new GameObjectQuery()
                .idEquals(id)
                .result(client)
                .stream()
                .filter(t -> t.getWorldLocation().isInArea(area))
                .findFirst()
                .orElse(null);
        return obj;
/*        WorldArea area2 = new WorldArea(new WorldPoint(client.getLocalPlayer().getWorldLocation().getX(),client.getLocalPlayer().getWorldLocation().getY() - idy,0)
                ,new WorldPoint(client.getLocalPlayer().getWorldLocation().getX(),client.getLocalPlayer().getWorldLocation().getY() + idy,0));
        GameObject obj2 = new GameObjectQuery()
                .idEquals(id)
                .result(client)
                .stream()
                .filter(t -> t.getWorldLocation().isInArea(area2))
                .findFirst()
                .orElse(null);
        return obj2;*/
    }
    private Point getLocation(TileObject tileObject) {
            if (tileObject == null) {
                return new Point(0, 0);
            }
            if (tileObject instanceof GameObject) {
                return ((GameObject) tileObject).getSceneMinLocation();
            }
            return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
        }
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
    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
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
        System.out.println("action = " + action + " butlerplanks = " + butlerplanks + " unnoting = "
                + unnoting + " buildstep = " + buildstep + " timeout = " + timeout + " shouldconsume = " + shouldConsume());
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

}