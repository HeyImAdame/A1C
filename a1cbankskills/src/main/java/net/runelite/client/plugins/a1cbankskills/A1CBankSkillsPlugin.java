package net.runelite.client.plugins.a1cbankskills;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.InventoryID;
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
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
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
    A1CBankSkillsConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(A1CBankSkillsConfig.class);
    }

    private String skillStage;
    private int timeout;
    private int isFirstDeposit;
    private int idprod;
    private int id1;
    private int id2;
    private int menuID = 0;
    private int amtID;
    private int craftNum;

    @Override
    protected void startUp()
    {
        timeout = 0;
        skillStage = null;
        isFirstDeposit = 1;
        idprod = 0;
        id1 = 0;
        id2 = 0;
        craftNum = 0;
    }
    @Override
    protected void shutDown()
    {
        timeout = 0;
        skillStage = null;
        isFirstDeposit = 1;
        idprod = 0;
        id1 = 0;
        id2 = 0;
        craftNum = 0;
    }
    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (idprod == 0 || id1 == 0 || id2 == 0 || craftNum == 0)
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
        if (client.getLocalPlayer().getAnimation() != -1
            && client.getLocalPlayer().getAnimation() != 6294)
        {
            timeout = 3;
        }
    }
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        updateConfig();
    }
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException
    {

        if (event.getMenuOption().equals("<col=00ff00>One Click Adam Bank Skillz"))
        {
            if (timeout > 50 || shouldConsume())
            {
                debug();
                event.consume();
                return;
            }
            if (outofMaterials())
            {
                sendGameMessage("No more materials found. Logging out in 15 seconds.");
                timeout = 75;
                return;
            }
            clickHandler(event);
            debug();
            return;
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event)
    {
        if (client.getLocalPlayer() == null
                || client.getGameState() != GameState.LOGGED_IN
                || client.getWidget(378, 78) != null)//login button
        {
            return;
        }

        String text = "<col=00ff00>One Click Adam Bank Skillz";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x -> x.getOption().equals(text)).findFirst().orElse(null));
    }

    private void clickHandler(MenuOptionClicked event)
    {
        if (skillStage == "useitemonitem" && isCraftingMenuOpen() && config.skill() != Types.Skill.CastSpell)
        {
            event.setMenuEntry(selectCraftOption());
            timeout = 1;
            skillStage = "pushcraftopt";
            return;
        }
        if (useItemOnItem() != null && config.skill() != Types.Skill.CastSpell)
        {
            event.setMenuEntry(useItemOnItem());
            timeout = 1;
            skillStage = "useitemonitem";
            return;
        }
        if (isInvFull() && config.skill() == Types.Skill.CastSpell)
        {
            event.setMenuEntry(castspell());
            timeout = 5;
            skillStage = "castspell";
            return;
        }
        if (!isbankOpen())
        {
            event.setMenuEntry(openBank());
            timeout = 1;
            skillStage = "openbank";
            return;
        }
        if (shouldDeposit())
        {
            if (isFirstDeposit == 1)
            {
                event.setMenuEntry(depositItems());
                isFirstDeposit = 0;
                skillStage = "depositall";
                return;
            }
        event.setMenuEntry(depositAllProducts());
        timeout = 1;
        skillStage = "depositprods";
        return;
        }
        if (getInventoryItem(id1) == null) {
            event.setMenuEntry(withdrawItem(id1, config.skill()));
            timeout = 1;
            skillStage = "withdrawid1";
            return;
        }
        if (getInventoryItem(id2) == null)
        {
            event.setMenuEntry(withdrawItem(id2, config.skill()));
            timeout = 1;
            skillStage = "withdrawid2";
            return;
        }
    }
    private MenuEntry openBank()
    {
        if (config.bankType() == Types.Banks.BOOTH)
        {
            GameObject gameObject = getGameObject(config.bankID());
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_SECOND_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    false);
        }

        if (config.bankType() == Types.Banks.CHEST)
        {
            GameObject gameObject = getGameObject(config.bankID());
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    false);
        }

        if (config.bankType() == Types.Banks.NPC)
        {
            NPC npc = getNpc(config.bankID());
            return createMenuEntry(
                    npc.getIndex(),
                    MenuAction.NPC_THIRD_OPTION,
                    getNPCLocation(npc).getX(),
                    getNPCLocation(npc).getY(),
                    false);
        }
        return null;
    }

    private MenuEntry depositItems()
    {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                786474,
                false);
    }

    private MenuEntry withdrawItem(Integer configID, Enum type)
    {
        if (getBankIndex(configID) == -1) return null;
        if (type == Types.Skill.Use1on27 || type == Types.Skill.CastSpell)
        {
            if (configID == id1)
            {
                amtID = 2;  //withdraw 1
            } else {
                amtID = 7; //withdraw all
            }
        }
        if (type == Types.Skill.Use14on14)
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
                true);
    }

    private MenuEntry useItemOnItem()
    {
        Widget item1 = getInventoryItem(id1);
        Widget item2 = getInventoryItem(id2);
        if (item1 == null || item2 == null) return null;
        setSelectedInventoryItem(item1);
        return createMenuEntry(0, MenuAction.WIDGET_TARGET_ON_WIDGET, item2.getIndex(), 9764864, true);
    }

    private void setSelectedInventoryItem(Widget item)
    {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getItemId());
    }

    private MenuEntry selectCraftOption()
    {
        menuID = 17694734 + craftNum - 1;
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                menuID,
                true);
    }

    private int getBankIndex(int id)
    {
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(id)
                .result(client)
                .first();
        if (bankItem == null) return -1;
        return bankItem.getWidget().getIndex();
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

    private GameObject getGameObject(int ID)
    {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getLocation(TileObject tileObject)
    {
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

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick)
    {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
    private boolean isInvEmpty()
    {
        List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
        return inventoryWidget.stream().allMatch(item -> item.getItemId() == 6512);
    }

    private boolean isInvFull()
    {
        List<Widget> inventoryWidget = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
        return (inventoryWidget.stream().noneMatch(item -> item.getItemId() == 6512));
    }
    private MenuEntry castspell() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                config.spelltype().getId(),
                false);
    }
    private boolean isbankOpen()
    {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private boolean isCraftingMenuOpen()
    {
        menuID = 17694734 + craftNum - 1;
        return client.getWidget(WidgetInfo.MULTI_SKILL_MENU) != null;
    }
    private boolean shouldDeposit()
    {
        if (((getInventoryItem(id1) == null
                && getInventoryItem(id2) == null)
                && isInvEmpty())
                || getInventoryItem(idprod) != null
                || ((getInventoryItem(id1) == null || getInventoryItem(id2) == null)
                && isInvFull()))
        {
            return true;
        }
        return false;
    }
    private boolean outofMaterials()
    {
        return ((getBankIndex(id1) == -1
                || getBankIndex(id2) == -1)
                && isbankOpen()
                && isInvEmpty());
    }
    private boolean shouldConsume()
    {
        if (!config.consumeMisclicks())
        {
            return false;
        }
        return ((client.getLocalPlayer().getAnimation() != -1
                && client.getLocalPlayer().getAnimation() != 6294)
                || getGameObject(config.bankID()) == null
                || timeout > 0
                || outofMaterials());
    }
    private void updateConfig()
    {
        if (config.product() == Types.Product.Custom)
        {
            isFirstDeposit = 1;
            craftNum = config.craftNum();
            idprod = config.customproductID();
            id1 = config.customingredientID1();
            id2 = config.customingredientID2();
            return;
        }
        isFirstDeposit = 1;
        craftNum = config.product().craftOpt;
        idprod = config.product().productid;
        id1 = config.product().ingredientid1;
        id2 = config.product().ingredientid2;
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
    private MenuEntry depositAllProducts()
    {
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
                true);
    }
    private void debug()
    {
        System.out.println("skillstage=" + skillStage + " timeout=" + timeout
                + " useItemonItem=" + (useItemOnItem() != null) + " isBankOpen="
                + isbankOpen() + " isInvEmpty=" + isInvEmpty() + " shouldconsume="
                + shouldConsume());
    }
}