package net.runelite.client.plugins.oneclickadamplanks;

import javax.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.InventoryID;
import net.runelite.api.NPC;
import net.runelite.api.ChatMessageType;
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
public class OneClickAdamPlanksPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private OneClickAdamPlanksConfig config;

    @Provides
    OneClickAdamPlanksConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickAdamPlanksConfig.class);
    }

    private int timeout = 0;
    private String action;
    private int logID = 0;
    private int plankID = 0;
    private int bankID = 0;
    private int forcelogout = 0;

    @Override
    protected void startUp() throws Exception
    {
        timeout = 0;
        action = "";
        logID = 0;
        plankID = 0;
        bankID = 0;
        forcelogout = 0;
    }

    @Subscribe
    public void OnGameTick(GameTick event)
    {
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
    public void onConfigChanged(ConfigChanged event)
    {
        updateConfig();
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (client.getLocalPlayer() == null
                || client.getGameState() != GameState.LOGGED_IN
                || client.getWidget(378, 78) != null) return;
        if (!(isInPOH() || isAtBank())) return;
        String text;
        {
            text =  "<col=00ff00>One Click Adam Plank Make";
        }

        client.insertMenuItem(
                text,
                "",
                MenuAction.UNKNOWN.getId(),
                0,
                0,
                0,
                true);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (timeout > 50)
        {
            event.consume();
            return;
        }
        if (outofMaterials() || forcelogout == 1)
        {
            sendGameMessage("No more materials found. Logging out in 15 seconds.");
            if (timeout > 40)
            {
                return;
            }
            timeout = 75;
            return;
        }
        if (event.getMenuOption().equals("<col=00ff00>One Click Adam Plank Make"))
        {
            System.out.println("action = " + action + " timeout = " + timeout + " isBankOpen = " + isbankOpen() + " shouldconsume = " + shouldConsume());
            handleClick(event);
            }
    }

    private void handleClick(MenuOptionClicked event)
    {
        if (shouldConsume())
        {
            event.consume();
            return;
        }
        if (!hasItems())
        {
            sendGameMessage("Missing items. Need at least 50k and law runes.");
            forcelogout = 1;
            return;
        }
        if (!goodSpellbook())
        {
            sendGameMessage("Wrong spellbook. Must be on standard.");
            forcelogout = 1;
            return;
        }

        if (isAtBank())
        {
            if (getEmptySlots() == 0 && (getItemSlots(logID) > 0))
            {
                event.setMenuEntry(teleportToHouseMES());
                timeout = 6;
                action = "House tele";
                return;
            }
            if (!isbankOpen())
            {
                event.setMenuEntry(openBank());
                timeout = 5;
                action = "Open bank";
                return;
            }
            if (getItemSlots(plankID) > 0)
            {
                event.setMenuEntry(depositPlanks());
                timeout = 1;
                action = "Depositing planks";
                return;
            }
            event.setMenuEntry(withdrawItem(logID));
            timeout = 1;
            action = "Withdraw item";
            return;
        }

        if (isInPOH())
        {
            if (!client.getWidget(162, 42).isHidden())
            {
                sendGameMessage("Butler not setup to take x26 to the sawmill.");
                 forcelogout = 1;
                 return;
            }
            if (client.getWidget(219, 1) != null)
            {
                if (!client.getWidget(WidgetInfo.DIALOG_OPTION_OPTION1).getChild(3).getText().contains("sawmill"))
                {
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

            if (client.getWidget(231, 5) != null)
            {
                event.setMenuEntry(clickContinueMES());
                timeout = 1;
                action = "clickContinue";
                return;
            }

            if (action == "callButler")
            {
                event.setMenuEntry(clickButler());
                timeout = 1;
                action = "clickButler";
                return;
            }

            if (client.getWidget(370, 19) != null && client.getWidget(370, 19).getChild(3) != null)
            {
                event.setMenuEntry(callButlerMES());
                timeout = 1;
                action = "callButler";
                return;
            }
            if (getItemSlots(logID) > 10)
            {
                if (client.getWidget(116, 8) != null)
                {
                    event.setMenuEntry(houseOptionsMES());
                    timeout = 1;
                    action = "houseOpts";
                    return;
                }
            }
            if (config.bank().ID == 4483)
            {
                event.setMenuEntry(useJewelryBoxMES());
                timeout = 8;
                action = "jewelryToBank";
                return;
            }
            event.setMenuEntry(teleToBank());
            timeout = 5;
            action = "spellbookToBank";
            return;
        }
    }
    private MenuEntry useJewelryBoxMES()
    {
        GameObject JewelryBox = getGameObject(29156);
        return createMenuEntry(JewelryBox.getId(),
                MenuAction.GAME_OBJECT_THIRD_OPTION,
                getLocation(JewelryBox).getX(),
                getLocation(JewelryBox).getY(),
                true);
    }
    private void enterPlankstoMake()
    {
        //client.invokeMenuAction();
        //getWidget(WidgetInfo.CHATBOX_FULL_INPUT);
        //client.getWidget(162, 42);

        return;
    }
    private MenuEntry teleportToHouseMES()
    {
        if (getInventoryItem(ItemID.CONSTRUCT_CAPE) != null)
        {
            return createMenuEntry(ItemID.CONSTRUCT_CAPE, MenuAction.ITEM_FOURTH_OPTION, getInventoryItem(ItemID.CONSTRUCT_CAPE).getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (getInventoryItem(ItemID.CONSTRUCT_CAPET) != null)
        {
            return createMenuEntry(ItemID.CONSTRUCT_CAPET, MenuAction.ITEM_FOURTH_OPTION, getInventoryItem(ItemID.CONSTRUCT_CAPET).getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                WidgetInfo.SPELL_TELEPORT_TO_HOUSE.getId(),
                true);
    }
    private MenuEntry teleToBank()
    {
        return client.createMenuEntry(
                "Seers'",
                "Camelot Teleport",
                2,
                MenuAction.CC_OP.getId(),
                -1,
                WidgetInfo.SPELL_TELEOTHER_CAMELOT.getId(),
                true);
    }
    private MenuEntry houseOptionsMES()
    {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                7602250,
                true);
    }
    private MenuEntry useLogsOnNPC()
    {
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
    private void setSelectedInventoryItem(Widget item)
    {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getItemId());
    }
    private MenuEntry callButlerMES()
    {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                24248339,
                true);
    }
    private MenuEntry clickButler()
    {
        NPC npc = getNpc(229);
        return createMenuEntry(
                npc.getIndex(),
                MenuAction.NPC_FIRST_OPTION,
                getNPCLocation(npc).getX(),
                getNPCLocation(npc).getY(),
                true);
    }
    private NPC getNpc(int id)
    {
        return new NPCQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    private Point getNPCLocation(NPC npc)
    {
        return new Point(npc.getLocalLocation().getSceneX(), npc.getLocalLocation().getSceneY());
    }
    private MenuEntry sendToSawmillMES(int chatOpt)
    {
        return createMenuEntry(
                0,
                MenuAction.WIDGET_CONTINUE,
                chatOpt,
                WidgetInfo.DIALOG_OPTION_OPTION1.getId(),
                true);
    }

    private MenuEntry clickContinueMES()
    {
        return createMenuEntry(
                0,
                MenuAction.WIDGET_CONTINUE,
                -1,
                15138821,
                true);
    }

    private GameObject getGameObject(int ID)
    {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getLocation(TileObject tileObject)
    {
        if (tileObject instanceof GameObject)
        {

            return ((GameObject) tileObject).getSceneMinLocation();
        }
        else
        {
            return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
        }
    }

    private Widget getWidgetItem(Widget widget, int id)
    {
        for (Widget item : widget.getDynamicChildren())
        {
            if (item.getItemId() == id)
            {
                return item;
            }
        }
        return null;
    }
    private int getItemSlots(int id)
    {
        List<Widget> inventory = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
        return (int) inventory.stream().filter(item -> item.getItemId() == id).count();
    }
    private int getEmptySlots()
    {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());

        if (inventory != null && !inventory.isHidden()
                && inventory.getDynamicChildren() != null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        if (bankInventory != null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren() != null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }
        return -1;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick)
    {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private boolean isAtBank()
    {
        return getGameObject(bankID) != null;
    }

    private boolean isInPOH()
    {
        return getGameObject(4525) != null; //checks for portal, p sure this is same for everyone if not need to do alternative check.
    }
    private void updateConfig()
    {
        bankID = config.bank().ID;
        logID = config.plank().ID1;
        plankID = config.plank().ID2;
        action = "";
        forcelogout = 0;
        return;
    }
    private int getBankIndex(int id)
    {
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
    private MenuEntry depositPlanks()
    {
        Widget item1 = getInventoryItem(plankID);
        if (item1 == null)
        {
            return null;
        }
        return createMenuEntry(
                8,
                MenuAction.CC_OP_LOW_PRIORITY,
                item1.getIndex(),
                983043,
                true);
    }
    private MenuEntry withdrawItem(Integer configID)
    {
        if (getBankIndex(configID) == -1)
        {
            return null;
        }

        return createMenuEntry(
                7,
                MenuAction.CC_OP,
                getBankIndex(configID),
                786445,
                true);
    }
    private boolean isbankOpen()
    {
        return client.getItemContainer(InventoryID.BANK) != null;
    }
    private MenuEntry openBank()
    {
        if (config.bank().Type == "Booth")
        {
            GameObject gameObject = getGameObject(config.bank().ID);
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_SECOND_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    true);
        }

        if (config.bank().Type == "Chest")
        {
            GameObject gameObject = getGameObject(config.bank().ID);
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    true);
        }
        return null;
    }
    private Widget getInventoryItem(int id)
    {
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
    private boolean outofMaterials()
    {
        return ((getBankIndex(logID) == -1
                && isbankOpen()
                && getEmptySlots() > 7)
                || forcelogout == 1);
    }
    private boolean shouldConsume()
    {
        if (!config.consumeMisclicks())
        {
            return false;
        }
        return (client.getLocalPlayer().getAnimation() != -1
                || timeout > 0
                || outofMaterials());
    }
    private void sendGameMessage(String message)
    {
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
    private void logout()
    {
        if (client.getWidget(69, 23) != null)
        {
            client.invokeMenuAction("Logout", "", 1, MenuAction.CC_OP.getId(), -1, WidgetInfo.WORLD_SWITCHER_LOGOUT_BUTTON.getId());
        }
        else
        {
            client.invokeMenuAction("Logout", "", 1, MenuAction.CC_OP.getId(), -1, 11927560);
        }
    }
    private boolean hasItems()
    {
        int coinInd = getInventoryItem(995).getIndex();
        int coinAmt = client.getWidget(WidgetInfo.INVENTORY).getChild(coinInd).getItemQuantity();
        int teleInd = getInventoryItem(563).getIndex();
        int teleAmt = client.getWidget(WidgetInfo.INVENTORY).getChild(teleInd).getItemQuantity();
        if (coinAmt > 50000 && teleAmt > 1)
        {
            return true;
        }
        return false;
    }
    private boolean goodSpellbook()
    {
        if (WidgetInfo.SPELLBOOK.getId() == 14286848)
        {
            return true;
        }
        return false;
    }
}