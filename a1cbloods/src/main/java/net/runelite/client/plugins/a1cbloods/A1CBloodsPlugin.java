package net.runelite.client.plugins.a1cbloods;
import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.WallObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import java.util.Arrays;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "A1C Bloods Morytania",
        description = "Craft Mory Bloods Better",
        enabledByDefault = false
)
@Slf4j
public class A1CBloodsPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private A1CBloodsConfig config;

    @Provides
    A1CBloodsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(A1CBloodsConfig.class);
    }

    private int runecraftingState = 0;
    private int bankingState = 0;
    private int cachedXP = 0;
    private boolean craftedRunes = false;
    private int alreadyClicked = 0;
    private int waitTicks = 0;
    private int test = 12312;
    private int justTeled = 0;


    @Override
    protected void startUp() throws Exception {
        reset();
    }

    @Subscribe
    private void onGameTick(GameTick event)
    {
        alreadyClicked = 0;
        if (waitTicks > 0)
        {
            waitTicks = waitTicks - 1;
        }

        if (cachedXP == 0)
        {
            cachedXP = client.getSkillExperience(Skill.RUNECRAFT);
        }

        //this is a patch i have no clue why the widget is triggering, some logic bug which i need to find but this is temporary solution.
        Widget widget = client.getWidget(229,1);
        if (config.noEssencePatch() && widget!=null && widget.getText().equals("You do not have any pure essences to bind."))
        {
            craftedRunes = true;
        }
    }

    private void reset() {
        runecraftingState = 0;
        bankingState = 0;
        craftedRunes = false;
        cachedXP = 0;
    }

    @Subscribe
    protected void onStatChanged(StatChanged event) {
        //on login this method triggers going from 0 to players current XP. all xp drops(even on leagues etc) should be below 50k and this method requires 77 rc.
        if (event.getSkill() == Skill.RUNECRAFT && event.getXp()- cachedXP <50000)
        {
            craftedRunes = true;
            cachedXP = client.getSkillExperience(Skill.RUNECRAFT);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getMessage().contains("There are no essences in this pouch."))
        {
            //not perfect but it works, prevents spam crafting if pouch is empty due to broken pouches previously
            craftedRunes = true ;
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event)
    {
        alreadyClicked = 0;

        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) return;
        String text = "<col=00ff00>One Click Bloods Morytania";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        //Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));

    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws Exception
    {
        if (event.getMenuOption().equals("<col=00ff00>One Click Bloods Morytania"))
            handleClick(event);
    }
    private void handleClick(MenuOptionClicked event) throws Exception//billion if statements but unsure of alternative method, can't assign menuentries until visible due to queries
    {
        if (shouldConsume() || waitTicks != 0)
        {
            event.consume();
            return;
        }

        Widget smallPouch = getInventoryItem(ItemID.SMALL_POUCH);
        Widget mediumPouch = getInventoryItem(ItemID.MEDIUM_POUCH);
        Widget largePouch = getInventoryItem(ItemID.LARGE_POUCH);
        Widget giantPouch = getInventoryItem(ItemID.GIANT_POUCH);
        Widget colossalPouch = getInventoryItem(ItemID.COLOSSAL_POUCH);

        if (handlePouchRepair()!=null)
        {
            event.setMenuEntry(handlePouchRepair());
            alreadyClicked = 1;
            return;
        }

        List<Integer> brokenPouches = Arrays.asList(ItemID.MEDIUM_POUCH_5511,ItemID.LARGE_POUCH_5513,ItemID.GIANT_POUCH_5515,ItemID.COLOSSAL_POUCH_26786);
        if (brokenPouches.stream().anyMatch(pouch -> client.getItemContainer(InventoryID.INVENTORY).contains(pouch)))
        {
            setEntry(event, createDarkMageMenuEntry());
            //event.setMenuEntry(repairPouchesSpellMES());
            alreadyClicked = 1;
            waitTicks = 3;
            return;
        }

        if (isInBloodAltar())
        {
            switch (runecraftingState)
            {
                case 0:
                    event.setMenuEntry(craftRunesMES());
                    alreadyClicked = 1;
                    if (!craftedRunes)
                    {
                        return;
                    }
                    craftedRunes = false;
                    runecraftingState = 1;
                case 1:
                    if (colossalPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(colossalPouch));
                        alreadyClicked = 1;
                        runecraftingState = 3;
                        return;
                    }
                    if (giantPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(giantPouch));
                        alreadyClicked = 1;
                        runecraftingState = 2;
                        return;
                    }
                case 2:
                    if (largePouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(largePouch));
                        alreadyClicked = 1;
                        runecraftingState = 3;
                        return;
                    }
                case 3:
                    event.setMenuEntry(craftRunesMES());
                    alreadyClicked = 1;
                    if (!craftedRunes)
                    {
                        return;
                    }
                    craftedRunes = false;
                    runecraftingState = 4;
                case 4:
                    if (colossalPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(colossalPouch));
                        alreadyClicked = 1;
                        runecraftingState = 6;
                        return;
                    }
                    if (mediumPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(mediumPouch));
                        alreadyClicked = 1;
                        runecraftingState = 5;
                        return;
                    }
                case 5:
                    if (smallPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(smallPouch));
                        alreadyClicked = 1;
                        runecraftingState = 6;
                        return;
                    }
                case 6:
                    event.setMenuEntry(craftRunesMES());
                    alreadyClicked = 1;
                    if (!craftedRunes)
                    {
                        return;
                    }
                    craftedRunes = false;
                    runecraftingState = 7;
                case 7:
                    if(justTeled == 0 || waitTicks == 0)
                    {
                        setEntry(event, createMoonclanTeleportMenuEntry());
                        alreadyClicked = 1;
                        waitTicks = 4;
                        justTeled = 1;
                        return;
                    }
                    //if (justTeled == 0 || waitTicks == 0)
                    //{
                    //    event.setMenuEntry(teleToBankMES());
                    //    alreadyClicked = 1;
                    //    waitTicks = 8;
                    //    justTeled = 1;
                    //    return;
                    //}

            }
        }

        if (bankOpen())
        {
            switch (bankingState)
            {
                case 0:
                    event.setMenuEntry(withdrawEssence());
                    alreadyClicked = 1;
                    bankingState = 1;
                    return;
                case 1:
                    if (colossalPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(colossalPouch));
                        alreadyClicked = 1;
                        bankingState = 3;
                        return;
                    }
                    if (giantPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(giantPouch));
                        alreadyClicked = 1;
                        bankingState = 2;
                        return;
                    }
                case 2:
                    if (largePouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(largePouch));
                        alreadyClicked = 1;
                        bankingState = 3;
                        return;
                    }
                case 3:
                    event.setMenuEntry(withdrawEssence());
                    alreadyClicked = 1;
                    bankingState = 4;
                    return;
                case 4:
                    if (colossalPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(colossalPouch));
                        alreadyClicked = 1;
                        bankingState = 6;
                        return;
                    }
                    if (mediumPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(mediumPouch));
                        alreadyClicked = 1;
                        bankingState = 5;
                        return;
                    }
                case 5:
                    if (smallPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(smallPouch));
                        alreadyClicked = 1;
                        bankingState = 6;
                        return;
                    }
                case 6:
                    event.setMenuEntry(withdrawEssence());
                    alreadyClicked = 1;
                    bankingState = 7;
                    justTeled = 0;
                    return;
                case 7:
                    Widget tab = getInventoryItem(ItemID.TELEPORT_TO_HOUSE);
                    createMenuEntry(2, MenuAction.CC_OP, tab.getIndex(), WidgetInfo.INVENTORY.getId(), false);
                    setEntry(event, itemEntry(getInventoryItem(ItemID.TELEPORT_TO_HOUSE), 2));
                    //event.setMenuEntry(teleToPOHMES());
                    alreadyClicked = 1;
                    waitTicks = 5;
                    return;
            }
        }

        if (isInBloodAltarArea())
        {
            event.setMenuEntry(enterAltarMES());
            alreadyClicked = 1;
            return;
        }

        if (isInPOH())
        {
            if (client.getEnergy()<config.runEnergy())
            {
                event.setMenuEntry(drinkFromPoolMES());
                alreadyClicked = 1;
                return;
            }
            event.setMenuEntry(useFairyRingMES());
            alreadyClicked = 1;
            waitTicks = 8;
            return;
        }
        if (isInMorytaniaHideout1())
        {
            event.setMenuEntry(leaveMorytaniaHideout1MES());
            alreadyClicked = 1;
            return;
        }
        if (isInMorytaniaHideout2())
        {
            event.setMenuEntry(leaveMorytaniaHideout2MES());
            alreadyClicked = 1;
            return;
        }
        if (isInMorytaniaHideout3())
        {
            event.setMenuEntry(leaveMorytaniaHideout3MES());
            alreadyClicked = 1;
            return;
        }
        if (isinMorytaniaHideout4LowAgility())
        {
            event.setMenuEntry(leaveMorytaniaHideout4LowAgilityMES());
            alreadyClicked = 1;
            return;
        }
        if (isinMorytaniaHideout5LowAgility())
        {
            event.setMenuEntry(useLowAgilityShortcut1MES());
            alreadyClicked = 1;
            return;
        }
        if (isinMorytaniaHideout5LowAgilityShortcut())
        {
            event.setMenuEntry(useLowAgilityShortcut2MES());
            alreadyClicked = 1;
            return;
        }

        if (isinMorytaniaHideout5HighAgilityShortcut())
        {
            event.setMenuEntry(useHighAgilityShortcut2MES());
            alreadyClicked = 1;
            return;
        }
        if (getEmptySlots()>0 && bankMES()!=null)
        {
            event.setMenuEntry(bankMES());
            alreadyClicked = 1;
            return;
        }
        if (justTeled == 0 || waitTicks == 0)
        {
            //setEntry(event, createMoonclanTeleportMenuEntry());
            //alreadyClicked = 1;
            //waitTicks = 8;
            //justTeled = 1;
           //return;
        }
        //if (getEmptySlots()!=0)
        //{
        //    event.setMenuEntry(teleToBankMES());
        //    alreadyClicked = 1;
        //    waitTicks = 8;
        //    return;
        //}
        Widget tab = getInventoryItem(ItemID.TELEPORT_TO_HOUSE);
        createMenuEntry(2, MenuAction.CC_OP, tab.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        setEntry(event, itemEntry(getInventoryItem(ItemID.TELEPORT_TO_HOUSE), 2));
        //event.setMenuEntry(teleToPOHMES());
        alreadyClicked = 1;
        waitTicks = 5;
    }

    private MenuEntry handlePouchRepair() {
        if (client.getWidget(231,6)!=null && client.getWidget(231, 6).getText().equals("What do you want? Can't you see I'm busy?"))
        {
            return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, -1, 15138821, false);
        }
        //if player doesn't have abyssal pouch in bank
        if (client.getWidget(219,1)!=null && client.getWidget(219,1).getChild(2)!=null && client.getWidget(219,1).getChild(2).getText().equals("Can you repair my pouches?"))
        {
            return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, 2, WidgetInfo.DIALOG_OPTION_OPTION1.getId(), false);
        }
        //if player has abyssal pouch in bank
        if (client.getWidget(219,1)!=null && client.getWidget(219,1).getChild(1)!=null && client.getWidget(219,1).getChild(1).getText().equals("Can you repair my pouches?"))
        {
            return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, 1, WidgetInfo.DIALOG_OPTION_OPTION1.getId(), false);
        }

        if (client.getWidget(217,6)!=null && client.getWidget(217,6).getText().equals("Can you repair my pouches?"))
        {
            return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, -1, 14221317, false);
        }
        return null;
    }

    private MenuEntry drinkFromPoolMES() {
        GameObject pool = getGameObject(29241);
        if (pool != null)
        {
            return client.createMenuEntry(
                    "Drink",
                    "Ornate pool of Rejuvenation",
                    pool.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(pool).getX(),
                    getLocation(pool).getY(),
                    true);
        }
        return null;
        //return createMenuEntry(pool.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(pool).getX(),getLocation(pool).getY(), false);
    }

    private MenuEntry useFairyRingMES() {
        GameObject fairyRing = getGameObject(29228);
        if (getGameObject(29229)!=null) //if tree fairy ring combo is present
        {
            fairyRing = getGameObject(29229);
            return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_FOURTH_OPTION, getLocation(fairyRing).getX(),getLocation(fairyRing).getY(), false);
        }
        if (fairyRing != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Fairy ring",
                    fairyRing.getId(),
                    MenuAction.GAME_OBJECT_THIRD_OPTION.getId(),
                    getLocation(fairyRing).getX(),
                    getLocation(fairyRing).getY(),
                    true);
        }
        return null;
        //return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_THIRD_OPTION, getLocation(fairyRing).getX(),getLocation(fairyRing).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout1MES() {
        GameObject tunnel = getGameObject(16308);
        if (tunnel != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    tunnel.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(tunnel).getX(),
                    getLocation(tunnel).getY(),
                    true);
        }
        return null;

        //return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }
    private MenuEntry leaveMorytaniaHideout2MES() {
        GameObject tunnel = getGameObject(5046);
        if (tunnel != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    tunnel.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(tunnel).getX(),
                    getLocation(tunnel).getY(),
                    true);
        }
        return null;
        //return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout3MES() {
        //if 93 agility & 78 mining use good shortcut else use shit one
        GameObject tunnel = getGameObject(43759); //new tunnel ID
        if ((client.getBoostedSkillLevel(Skill.AGILITY)<93 || client.getBoostedSkillLevel(Skill.MINING)<78) && !config.overrideAgility())
        {
            tunnel = getGameObject(12770);
        }
        if (tunnel != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    tunnel.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(tunnel).getX(),
                    getLocation(tunnel).getY(),
                    true);
        }
        return null;
        //return createMenuEntry( tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout4LowAgilityMES() {
        //multiple objects with same ID so need to ensure it's the south tunnel
        WorldArea worldarea = new WorldArea(new WorldPoint(3488,9858,0),new WorldPoint(3495,9865,0));
        GameObject tunnel = new GameObjectQuery()
                .idEquals(12771)
                .result(client)
                .stream()
                .filter(t -> t.getWorldLocation().isInArea(worldarea))
                .findFirst()
                .orElse(null);
        if (tunnel != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    tunnel.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(tunnel).getX(),
                    getLocation(tunnel).getY(),
                    true);
        }
        return null;
        //return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry useLowAgilityShortcut1MES() {
        WallObject tunnel = new WallObjectQuery()
                .idEquals(43755)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        if (tunnel != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    tunnel.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(tunnel).getX(),
                    getLocation(tunnel).getY(),
                    true);
        }
        return null;
        //return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }
    private MenuEntry useLowAgilityShortcut2MES() {
        WallObject tunnel = new WallObjectQuery()
                .idEquals(43758)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        if (tunnel != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    tunnel.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(tunnel).getX(),
                    getLocation(tunnel).getY(),
                    true);
        }
        return null;
        //return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry useHighAgilityShortcut2MES() {
        WallObject tunnel = new WallObjectQuery()
                .idEquals(43762)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        if (tunnel != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    tunnel.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(tunnel).getX(),
                    getLocation(tunnel).getY(),
                    true);
        }
        return null;
        //return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry enterAltarMES() {
        GameObject altar = getGameObject(25380);
        if (getInventoryItem(ItemID.CATALYTIC_TALISMAN)!=null)
        {
            return useItemOnAltarMES(altar, getInventoryItem(ItemID.CATALYTIC_TALISMAN));
        }
        if (getInventoryItem(ItemID.BLOOD_TALISMAN)!=null)
        {
            return useItemOnAltarMES(altar, getInventoryItem(ItemID.BLOOD_TALISMAN));
        }
        //else assume something is worn giving access to altar
        if (altar != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    altar.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getLocation(altar).getX(),
                    getLocation(altar).getY(),
                    true);
        }
        return null;
        //return createMenuEntry(altar.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(altar).getX(), getLocation(altar).getY(), false);
    }

    private MenuEntry useItemOnAltarMES(GameObject altar,Widget item) {
        setSelectedInventoryItem(item);
        return createMenuEntry(altar.getId(), MenuAction.ITEM_USE_ON_GAME_OBJECT, getLocation(altar).getX(), getLocation(altar).getY(), false);
    }

    private void setSelectedInventoryItem(Widget item) {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getId());
    }

    private MenuEntry craftRunesMES() {
        GameObject altar = getGameObject(43479);
        return createMenuEntry(altar.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(altar).getX(), getLocation(altar).getY(), true);
    }

    private MenuEntry emptyPouchMES(Widget pouch) {
        return createMenuEntry(3, MenuAction.CC_OP, pouch.getIndex(), WidgetInfo.INVENTORY.getId(), false);
    }

    private MenuEntry teleToBankMES() {
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE_13342))
        {
            return createMenuEntry(4, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CRAFTING_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CRAFTING_CAPET))
        {
            return createMenuEntry(3, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }

        Widget craftingCape = getInventoryItem(ItemID.CRAFTING_CAPE);
        Widget craftingCapeT = getInventoryItem(ItemID.CRAFTING_CAPET);
        if (craftingCape!=null)
        {
            return createMenuEntry(4, MenuAction.CC_OP, craftingCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (craftingCapeT!=null)
        {
            return createMenuEntry(4, MenuAction.CC_OP, craftingCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (client.getVarbitValue(4070)==0) //if on standard spellbook
        {
            return createMenuEntry(2, MenuAction.CC_OP, -1, WidgetInfo.SPELL_CAMELOT_TELEPORT.getId(), false);
        }
            return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_MOONCLAN_TELEPORT.getId(), false);
    }

    private MenuEntry bankMES() {
        GameObject craftingBank = getGameObject(14886);
        if (craftingBank!=null)
        {
            return createMenuEntry(craftingBank.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(craftingBank).getX(), getLocation(craftingBank).getY(), false);
        }
        GameObject lunarBank = getGameObject(16700);
        if (lunarBank!=null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Bank booth",
                    lunarBank.getId(),
                    MenuAction.GAME_OBJECT_SECOND_OPTION.getId(),
                    getLocation(lunarBank).getX(),
                    getLocation(lunarBank).getY(),
                    true);
            //return createMenuEntry(lunarBank.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, getLocation(lunarBank).getX(), getLocation(lunarBank).getY(), false);
        }
        GameObject seersBank = getGameObject(25808);
        if (seersBank!=null)
        {
            return createMenuEntry(seersBank.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, getLocation(seersBank).getX(), getLocation(seersBank).getY(), false);
        }
        return null;
    }

    private MenuEntry withdrawEssence() {
        int essence = ItemID.PURE_ESSENCE;
        if (config.essenceType()== EssenceType.DAEYALT_ESSENCE)
        {
            essence = ItemID.DAEYALT_ESSENCE;
        }
        return createMenuEntry(7, MenuAction.CC_OP_LOW_PRIORITY, getBankIndex(essence), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
    }

    private MenuEntry fillPouchMES(Widget pouch) {
        return createMenuEntry(9, MenuAction.CC_OP_LOW_PRIORITY, pouch.getIndex(), WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), false);
    }

    private MenuEntry teleToPOHMES() {
        Widget tab = getInventoryItem(ItemID.TELEPORT_TO_HOUSE);
        Widget conCape = getInventoryItem(ItemID.CONSTRUCT_CAPE);
        Widget conCapeT = getInventoryItem(ItemID.CONSTRUCT_CAPET);

        if (conCape!=null)
        {
            return createMenuEntry(6, MenuAction.CC_OP_LOW_PRIORITY, conCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (conCapeT!=null)
        {
            return createMenuEntry(6, MenuAction.CC_OP_LOW_PRIORITY, conCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (tab!=null)
        {
            return createMenuEntry(2, MenuAction.CC_OP, tab.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT)!=null)
        {
            if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE_13342))
            {
                return createMenuEntry(5, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
            }
            if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CONSTRUCT_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CONSTRUCT_CAPET))
            {
                return createMenuEntry( 4, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
            }
        }
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_TELEPORT_TO_HOUSE.getId(), false);
    }

    private MenuEntry repairPouchesSpellMES() {
        return createMenuEntry(2, MenuAction.CC_OP, -1, WidgetInfo.SPELL_NPC_CONTACT.getId(), false);
    }

    private boolean isInPOH() {
        reset();
        return getGameObject(4525)!=null; //checks for portal, p sure this is same for everyone if not need to do alternative check.
    }
    private boolean isInMorytaniaHideout1() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3437,9819,0),new WorldPoint(3454,9830,0)));
    }

    private boolean isInMorytaniaHideout2() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3457,9807,0),new WorldPoint(3475,9825,0)));
    }

    private boolean isInMorytaniaHideout3() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3476,9799,0),new WorldPoint(3507,9840,0)));
    }

    private boolean isinMorytaniaHideout4LowAgility() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3485,9859,0),new WorldPoint(3498,9879,0)));
    }

    private boolean isinMorytaniaHideout5LowAgility() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3511,9807,0),new WorldPoint(3538,9832,0)))
                || client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3536,9811,0),new WorldPoint(3563,9832,0)));
    }

    private boolean isinMorytaniaHideout5LowAgilityShortcut() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3546,9785,0),new WorldPoint(3572,9812,0)));
    }

    private boolean isinMorytaniaHideout5HighAgilityShortcut() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3532,9764,0),new WorldPoint(3541,9781,0)));
    }

    private boolean isInBloodAltarArea() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3542,9764,0),new WorldPoint(3570,9784,0)));
    }

    private boolean isInBloodAltar() {
        int BLOOD_ALTAR_ID = 43479;
        return getGameObject(BLOOD_ALTAR_ID)!=null;
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
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

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
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

    private Widget getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return getWidgetItem(inventoryWidget,id);
        }
        if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
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

    private MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
            return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                    .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private boolean shouldConsume(){
        if (!config.consumeMisclicks())
            return false;
        return (client.getLocalPlayer().isMoving()
                || client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation()
                || client.getLocalPlayer().getAnimation() != -1
                || alreadyClicked == 1
                || alreadyClicked > 0
                || waitTicks != 0
                || client.getLocalPlayer().getAnimation() == 1816
                || client.getLocalPlayer().getAnimation() == 2796
                || client.getLocalPlayer().getAnimation() == 4413
                || client.getLocalPlayer().getAnimation() == 4069
                || client.getLocalPlayer().getAnimation() == 4071
                || client.getLocalPlayer().getAnimation() == 3265
                || client.getLocalPlayer().getAnimation() == 3266)
                && client.getLocalPlayer().getAnimation() != 791;
    }

    private MenuEntry createMoonclanTeleportMenuEntry()
    {
        return client.createMenuEntry(
                "Cast",
                "Moonclan Teleport",
                1,
                MenuAction.CC_OP.getId(),
                -1,
                WidgetInfo.SPELL_MOONCLAN_TELEPORT.getId(),
                true);
    }

    private MenuEntry createDarkMageMenuEntry()
    {
        return client.createMenuEntry(
                "Dark Mage",
                "NPC Contact",
                2,
                MenuAction.CC_OP.getId(),
                -1,
                WidgetInfo.SPELL_NPC_CONTACT.getId(),
                true);
    }

    public void setEntry(MenuOptionClicked event, MenuEntry entry)
    {
        try
        {
            event.setMenuOption(entry.getOption());
            event.setMenuTarget(entry.getTarget());
            event.setId(entry.getIdentifier());
            event.setMenuAction(entry.getType());
            event.setParam0(entry.getParam0());
            event.setParam1(entry.getParam1());
        }
        catch (Exception e)
        {
            event.consume();
        }
    }

    public MenuEntry itemEntry(Widget item, int action)
    {
        if (item == null)
            return null;

        return client.createMenuEntry(
                "",
                "",
                action,
                action < 6 ? MenuAction.CC_OP.getId() : MenuAction.CC_OP_LOW_PRIORITY.getId(),
                item.getIndex(),
                WidgetInfo.INVENTORY.getId(),
                false
        );
    }



}