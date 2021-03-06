package me.Cutiemango.MangoQuest.manager;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.TextComponentFactory;
import me.Cutiemango.MangoQuest.model.InteractiveText;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.model.QuestBookPage;
import me.Cutiemango.MangoQuest.model.QuestTrigger;
import me.Cutiemango.MangoQuest.model.QuestTrigger.TriggerObject;
import me.Cutiemango.MangoQuest.model.QuestTrigger.TriggerType;
import me.Cutiemango.MangoQuest.model.RequirementType;
import me.Cutiemango.MangoQuest.questobjects.ItemObject;
import me.Cutiemango.MangoQuest.questobjects.NumerableObject;
import me.Cutiemango.MangoQuest.questobjects.QuestObjectBreakBlock;
import me.Cutiemango.MangoQuest.questobjects.QuestObjectKillMob;
import me.Cutiemango.MangoQuest.questobjects.QuestObjectReachLocation;
import me.Cutiemango.MangoQuest.questobjects.QuestObjectTalkToNPC;
import me.Cutiemango.MangoQuest.questobjects.QuestObjectDeliverItem;
import me.Cutiemango.MangoQuest.questobjects.SimpleQuestObject;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestEditorManager {

	public static HashMap<String, Quest> isEditing = new HashMap<>();
	
	public static boolean isInEditorMode(Player p){
		return isEditing.containsKey(p.getName());
	}
	
	public static Quest getCurrentEditingQuest(Player p){
		return isEditing.get(p.getName());
	}
	
	public static void edit(Player p, Quest q){
		isEditing.put(p.getName(), q);
		editQuest(p);
	}
	
	public static void exit(Player p){
		isEditing.remove(p.getName());
		QuestUtil.info(p, "&a已經退出了編輯模式。");
	}
	
	public static void mainGUI(Player p){
		QuestBookPage p1 = new QuestBookPage();
		p1.add("&9&l任務線上編輯器系統 &0&l：").changeLine();
		p1.add(new InteractiveText("&0&l- 《新增任務》")
				.clickCommand("/mq e newquest").showText("&f點擊以新增任務")).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText("&0&l- 《編輯任務》")
				.clickCommand("/mq e edit").showText("&f點擊進入編輯任務介面")).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText("&0&l- 《移除任務》")
				.clickCommand("/mq e remove").showText("&f點擊進入移除任務介面")).changeLine();
		p1.changeLine();
		
		if (isInEditorMode(p) && QuestEditorManager.getCurrentEditingQuest(p).getInternalID() != null){
			p1.add(new InteractiveText("&c&l- 《返回編輯》")
					.clickCommand("/mq e gui").showText("&e您還有尚未儲存的任務！")).changeLine();
			p1.changeLine();
			p1.add(new InteractiveText("&c&l- 《退出編輯》")
					.clickCommand("/mq e exit").showText("&c&l所做的任何變更都不會儲存。")).changeLine();
		}
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void editGUI(Player p){
		QuestBookPage p1 = new QuestBookPage();
		p1.add("&9&l任務線上編輯器系統&0&l： 選擇要&2&l編輯&0&l的任務").changeLine();
		for (Quest q : QuestStorage.Quests.values()){
			p1.add(new InteractiveText("&0- &l" + q.getQuestName() + "&0(" + q.getInternalID() + ")")
					.clickCommand("/mq e select " + q.getInternalID()));
			p1.changeLine();
		}
		p1.changeLine();
		p1.add(new InteractiveText("&0&l[返回編輯器選單]").clickCommand("/mq e"));
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void removeGUI(Player p){
		QuestBookPage p1 = new QuestBookPage();
		p1.add("&9&l任務線上編輯器系統&0&l： 選擇要&c&l移除&0&l的任務").changeLine();
		for (Quest q : QuestStorage.Quests.values()){
			p1.add(new InteractiveText("&0- &l" + q.getQuestName() + "&0(" + q.getInternalID() + ")")
					.clickCommand("/mq e remove confirm " + q.getInternalID()));
			p1.changeLine();
		}
		p1.changeLine();
		p1.add(new InteractiveText("&0&l[返回編輯器選單]").clickCommand("/mq e"));
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void removeConfirmGUI(Player p, Quest q){
		QuestBookPage p1 = new QuestBookPage();
		p1.add("&4&l你確定要刪除任務 ").changeLine();
		p1.add("&0&l" + q.getQuestName() + " &4&l的所有資料嗎？").changeLine();
		p1.add("&8&l(不可回復，請審慎考慮)").changeLine();
		p1.changeLine();
		p1.changeLine();
		p1.add("  ");
		p1.add(new InteractiveText("&2&l【確定】").clickCommand("/mq e remove quest " + q.getInternalID()));
		p1.add(" &8&l/ ");
		p1.add(new InteractiveText("&c&l【取消】").clickCommand("/mq e remove"));
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void editQuest(Player p){
		if (!QuestEditorManager.isInEditorMode(p)){
			QuestUtil.error(p, "你不在編輯模式中！");
			return;
		}
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		
		QuestBookPage p1 = new QuestBookPage();
		p1.add("&d&l基本資料編輯》 ").changeLine();
		p1.add("&0&l任務編碼： &0" + q.getInternalID()).changeLine();
		
		p1.add(new InteractiveText("&6&l任務名稱： &0").clickCommand("/mq e edit name").showText("&f點擊以編輯 任務名稱"));
		p1.add(q.getQuestName()).changeLine();
		p1.add(new InteractiveText("&0&l任務NPC&0&l： ").clickCommand("/mq e edit npc").showText("&f點擊以編輯 任務NPC"));
		if (q.isCommandQuest())
			p1.add("&c&l無");
		else
			p1.add(TextComponentFactory.convertLocHoverEvent(q.getQuestNPC().getName(), q.getQuestNPC().getEntity().getLocation(), false));
		p1.changeLine();
		
		if (q.isRedoable()){
			p1.add(new InteractiveText("&0&l重複執行&0&l： ").clickCommand("/mq e edit redo false").showText("&f點擊以變更 &c不可重複執行"));
			p1.add("&a是").changeLine();
			p1.add(new InteractiveText("&0&l等待時間&0&l： ").clickCommand("/mq e edit redodelay").showText("&f點擊以編輯 重複執行等待時間"));
			p1.add(QuestUtil.convertTime(q.getRedoDelay())).changeLine();
		}
		else{
			p1.add(new InteractiveText("&0&l重複執行&0&l： ").clickCommand("/mq e edit redo true").showText("&f點擊以變更 &c不可重複執行"));
			p1.add("&c否").changeLine();
		}
		
		QuestBookPage p2 = new QuestBookPage();
		p2.add("&2&l需求/事件/階段編輯》").changeLine();
		
		p2.add("&0任務&3&l需求：").changeLine();
		p2.add(new InteractiveText("[前往編輯頁面]")
				.clickCommand("/mq e edit req").showText("&f點擊以前往 &3任務需求&f編輯頁面")).changeLine();
		p2.add("&0任務&5&l事件： ").changeLine();
		p2.add(new InteractiveText("[前往編輯頁面]")
				.clickCommand("/mq e edit evt").showText("&f點擊以前往 &5任務事件&f編輯頁面")).changeLine();
		p2.add("&0任務&c&l階段： ").changeLine();
		p2.add(new InteractiveText("[前往編輯頁面]")
				.clickCommand("/mq e edit stage").showText("&f點擊以前往 &c任務階段&f編輯頁面")).changeLine();
		
		
		QuestBookPage p3 = new QuestBookPage();
		p3.add("&8&l任務提要》").changeLine();
		for (String out : q.getQuestOutline()){
			p3.add(out).changeLine();
		}
		p3.changeLine();
		p3.add(new InteractiveText("&0&l[編輯提要]").clickCommand("/mq e edit outline"));
		
		QuestBookPage p4 = new QuestBookPage();
		p4.add("&6&l任務獎勵》").changeLine();

		p4.add("金錢： " + q.getQuestReward().getMoney() + " 元");
		p4.add(new InteractiveText("&0 &l[編輯]").clickCommand("/mq e edit reward money").showText("&f點擊以輸入 &6金錢獎勵")).changeLine();
		
		p4.add("經驗值： " + q.getQuestReward().getExp() + " 點");
		p4.add(new InteractiveText("&0 &l[編輯]").clickCommand("/mq e edit reward exp").showText("&f點擊以輸入 &b經驗值獎勵")).changeLine();

		p4.add(new InteractiveText("好感度： ").clickCommand("/mq e addnew reward fp").showText("&f點擊以&c新增&f好感度設定")).changeLine();
		if (q.getQuestReward().hasFriendPoint()) {
			for (Integer n : q.getQuestReward().getFp().keySet()) {
				NPC npc = CitizensAPI.getNPCRegistry().getById(n);
				if (npc == null)
					continue;
				p4.add("- ");
				p4.add(TextComponentFactory.convertLocHoverEvent(npc.getName(), npc.getEntity().getLocation(), false));
				p4.add("&0 " + q.getQuestReward().getFp().get(n) + " 點");
				p4.add(new InteractiveText("&0&l[編輯]").clickCommand("/mq e edit reward fp " + n));
				p4.add(new InteractiveText("&0&l[移除]").clickCommand("/mq e remove reward fp " + n));
				p4.changeLine();
			}
		}
		p4.add(new InteractiveText("指令： ")
				.clickCommand("/mq e addnew reward command").showText("&f點擊以&c新增&f指令")).changeLine();
		if (q.getQuestReward().hasCommand()){
			int counter = 1;
			for (String s : q.getQuestReward().getCommands()){
				p4.add(new InteractiveText("- &0指令(" + counter + ")").showText("&f/" + s));
				p4.add(new InteractiveText("&0&l[編輯]").clickCommand("/mq e edit reward command " + (counter-1)));
				p4.add(new InteractiveText("&0&l[移除]").clickCommand("/mq e remove reward command " + (counter-1)));
				p4.changeLine();
				counter++;
			}
		}
		p4.add("物品： ").changeLine();
		p4.add(new InteractiveText("&7[點擊開啟GUI]").clickCommand("/mq e edit reward item").showText("&f請將物品丟入物品欄，系統將會自動讀取。")).changeLine();
		
		QuestBookPage p5 = new QuestBookPage();
		p5.add("&c&l儲存/退出》").changeLine();
		p5.add(new InteractiveText("&2&l【同步伺服器與設定檔】")
				.clickCommand("/mq e sa").showText("&c&l注意： 目前持有此任務的玩家都會遺失任務進度。")).changeLine();
		p5.add(new InteractiveText("&5&l【同步伺服器】")
				.clickCommand("/mq e sl").showText("&c&l注意： 目前持有此任務的玩家都會遺失任務進度。")).changeLine();
		p5.add(new InteractiveText("&9&l【同步設定檔】")
				.clickCommand("/mq e sc").showText("&c&l注意： 目前持有此任務的玩家都會遺失任務進度。")).changeLine();
		p5.changeLine();
		p5.add(new InteractiveText("&0&l[退出編輯器]")
				.clickCommand("/mq e exit").showText("&c&l所做的任何變更都不會儲存。")).changeLine();

		QuestGUIManager.openBook(p, p1, p2, p3, p4, p5);
	}
	
	public static void editQuestTrigger(Player p){
		if (!QuestEditorManager.isInEditorMode(p)){
			QuestUtil.error(p, "你不在編輯模式中！");
			return;
		}
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		QuestBookPage p1 = new QuestBookPage();
		p1.add("&0&l編輯任務事件： " + q.getQuestName()).changeLine();
		int index = 0;
		for (QuestTrigger qt : q.getTriggers()){
			p1.add("- " + index + ".");
			if (qt.getType().equals(TriggerType.TRIGGER_STAGE_START)
					|| qt.getType().equals(TriggerType.TRIGGER_STAGE_FINISH)) {
				p1.add(new InteractiveText(qt.getTriggerObject().toCustomString()).showText(
						"觸發時機： " + qt.getType().toCustomString(qt.getCount()) + "\n觸發物件內容： "
								+ qt.getObject().toString()));
				p1.add(new InteractiveText("&7[編輯]").clickCommand("/mq e edit evt " + index + " "
						+ qt.getType().toString() + " " + qt.getCount() + " " + qt.getTriggerObject().toString()));
			} else {
				p1.add(new InteractiveText(qt.getTriggerObject().toCustomString()).showText(
						"觸發時機： " + qt.getType().toCustomString() + "\n觸發物件內容： " + qt.getObject().toString()));
				p1.add(new InteractiveText("&7[編輯]").clickCommand("/mq e edit evt " + index + " "
						+ qt.getType().toString() + " " + qt.getTriggerObject().toString()));
			}
			p1.add(new InteractiveText("&7[移除]").clickCommand("/mq e remove evt " + index));
			p1.changeLine();
			index++;
		}
		p1.add(new InteractiveText("&0&l[新增]").clickCommand("/mq e addnew evt"));
		p1.changeLine();
		p1.add(new InteractiveText("&0&l[返回任務選單]").clickCommand("/mq e gui"));
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void editQuestStages(Player p){
		if (!QuestEditorManager.isInEditorMode(p)){
			QuestUtil.error(p, "你不在編輯模式中！");
			return;
		}
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l編輯任務內容： "));
		p1.addExtra("\n");
		p1.addExtra(QuestUtil.translateColor("&c(選擇要編輯的任務階段。)\n"));
		for (int i = 1; i <= q.getStages().size(); i++){
			p1.addExtra(TextComponentFactory.regClickCmdEvent(
					QuestUtil.translateColor("[編輯階段(" + i + ")]"), "/mq e edit stage " + i));
			p1.addExtra(TextComponentFactory.regClickCmdEvent(
					QuestUtil.translateColor("&c [移除]"), "/mq e remove stage " + i));
			p1.addExtra("\n");
		}
		p1.addExtra(TextComponentFactory.regClickCmdEvent(
				QuestUtil.translateColor("&c&l[新增]"), "/mq e addnew stage"));
		p1.addExtra("\n");
		p1.addExtra(TextComponentFactory.regClickCmdEvent("&0&l[返回任務選單]", "/mq e gui"));
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void editQuestObjects(Player p, int stage){
		if (!QuestEditorManager.isInEditorMode(p)){
			QuestUtil.error(p, "你不在編輯模式中！");
			return;
		}
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l編輯：任務階段(" + stage + ")"));
		p1.addExtra("\n");
		p1.addExtra(QuestUtil.translateColor("&c(選擇要編輯的任務目標。)\n"));
		for (int i = 1; i <= q.getStage(stage - 1).getObjects().size(); i++){
			p1.addExtra(TextComponentFactory.regClickCmdEvent(
					QuestUtil.translateColor("[編輯目標(" + i + ")]"), "/mq e edit object " + stage + " " + i));
			p1.addExtra(TextComponentFactory.regClickCmdEvent(
					QuestUtil.translateColor("&c [移除]"), "/mq e remove object " + stage + " " + i));
			p1.addExtra("\n");
			
		}
		p1.addExtra(TextComponentFactory.regClickCmdEvent(
				QuestUtil.translateColor("&c&l[新增]"), "/mq e addnew object " + stage));
		p1.addExtra("\n");
		p1.addExtra(TextComponentFactory.regClickCmdEvent("&0&l[返回選擇任務階段]", "/mq e edit stage"));
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void editQuestObject(Player p, int stage, int obj){
		if (!QuestEditorManager.isInEditorMode(p)){
			QuestUtil.error(p, "你不在編輯模式中！");
			return;
		}
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		SimpleQuestObject o = q.getStage(stage - 1).getObject(obj - 1);
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l編輯：任務目標(" + stage + "-" + obj + ")"));
		p1.addExtra("\n");
		p1.addExtra(QuestUtil.translateColor("&c&l目標類別：&0 " + o.getObjectName()));
		p1.addExtra("\n");
		p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " type"));
		p1.addExtra("\n");
		switch(o.getConfigString()){
			case "BREAK_BLOCK":
				p1.addExtra(QuestUtil.translateColor("&0破壞方塊： " + QuestUtil.translate(((QuestObjectBreakBlock)o).getType(), ((QuestObjectBreakBlock)o).getShort())));
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " block"));
				p1.addExtra("\n");
				break;
			case "CONSUME_ITEM":
				p1.addExtra(QuestUtil.translateColor("&0消耗物品： "));
				p1.addExtra(TextComponentFactory.convertItemHoverEvent(((ItemObject)o).getItem(), false));
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " item"));
				p1.addExtra("\n");
				break;
			case "DELIVER_ITEM":
				p1.addExtra(QuestUtil.translateColor("&0提交物品： "));
				p1.addExtra(TextComponentFactory.convertItemHoverEvent(((ItemObject)o).getItem(), false));
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " item"));
				p1.addExtra("\n");
				p1.addExtra(QuestUtil.translateColor("&0提交NPC： "));
				NPC npc = ((QuestObjectDeliverItem)o).getTargetNPC();
				if (npc == null)
					p1.addExtra(TextComponentFactory.regHoverEvent("&0【未設定的NPC】", "&c請盡快更換此任務NPC，因為不存在！"));
				else
					p1.addExtra(TextComponentFactory.convertLocHoverEvent(npc.getName(), npc.getEntity().getLocation(), false));
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " itemnpc"));
				p1.addExtra("\n");
				break;
			case "KILL_MOB":
				if (Main.instance.initManager.hasMythicMobEnabled()){
					p1.addExtra(QuestUtil.translateColor("&0自定義MTM怪物名稱： "));
					if (((QuestObjectKillMob)o).isMythicObject())
						p1.addExtra(((QuestObjectKillMob)o).getMythicMob().getDisplayName() +
								"(" + ((QuestObjectKillMob)o).getMythicMob().getInternalName() + ")");
					else
						p1.addExtra(QuestUtil.translateColor("&c未設定"));
					p1.addExtra("\n");
					p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " mtmmob"));
					p1.addExtra("\n");
				}
				p1.addExtra(QuestUtil.translateColor("&0自定義怪物名稱： "));
				if (((QuestObjectKillMob)o).hasCustomName())
					p1.addExtra(QuestUtil.translateColor(((QuestObjectKillMob)o).getCustomName()));
				else
					p1.addExtra(QuestUtil.translateColor("&c未設定"));
				p1.addExtra("\n");
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " mobname"));
				p1.addExtra("\n");
				
				p1.addExtra(QuestUtil.translateColor("&0怪物類別： " + QuestUtil.translate(((QuestObjectKillMob)o).getType())));
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " mobtype"));
				p1.addExtra("\n");
				break;
			case "REACH_LOCATION":
				p1.addExtra(QuestUtil.translateColor("&0到達地點： "));
				p1.addExtra(QuestUtil.translateColor(
						"(" + ((QuestObjectReachLocation)o).getLocation().getBlockX() +
						", " + ((QuestObjectReachLocation)o).getLocation().getBlockY() +
						", " + ((QuestObjectReachLocation)o).getLocation().getBlockZ() + ")"));
				p1.addExtra("\n");
				p1.addExtra(QuestUtil.translateColor("&0到達範圍： " + ((QuestObjectReachLocation)o).getRadius() + " 格"));
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " loc"));
				p1.addExtra("\n");
				p1.addExtra(QuestUtil.translateColor("&0到達地點名稱： " + ((QuestObjectReachLocation)o).getName()));
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " locname"));
				p1.addExtra("\n");
				break;
			case "TALK_TO_NPC":
				p1.addExtra(QuestUtil.translateColor("&0NPC： "));
				npc = ((QuestObjectTalkToNPC)o).getTargetNPC();
				if (npc == null)
					p1.addExtra(TextComponentFactory.regHoverEvent("&0【未設定的NPC】", "&c請盡快更換此任務NPC，因為不存在！"));
				else
					p1.addExtra(TextComponentFactory.convertLocHoverEvent(npc.getName(), npc.getEntity().getLocation(), false));
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " npc"));
				p1.addExtra("\n");
				break;
				
		}
		if (o instanceof NumerableObject){
			p1.addExtra(QuestUtil.translateColor("&0目標數量： " + ((NumerableObject)o).getAmount()));
			p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e edit object " + stage + " " + obj + " amount"));
			p1.addExtra("\n");
		}
		p1.addExtra("\n");
		p1.addExtra(TextComponentFactory.regClickCmdEvent("&0&l[返回選擇任務目標]", "/mq e edit stage " + stage));
		QuestGUIManager.openBook(p, p1);
		return;
	}
	
	@SuppressWarnings("unchecked")
	public static void editQuestRequirement(Player p){
		if (!QuestEditorManager.isInEditorMode(p)){
			QuestUtil.error(p, "你不在編輯模式中！");
			return;
		}
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l編輯任務需求： " + q.getQuestName()));
		TextComponent p2 = new TextComponent(QuestUtil.translateColor(""));
		p1.addExtra("\n");
		for (RequirementType t : RequirementType.values()){
			switch(t){
			case ITEM:
				p1.addExtra("物品需求：");
				p1.addExtra("\n");
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&7[點擊開啟GUI]", "/mq e edit req ITEM"));
				int i = 0;
				p1.addExtra("\n");
				break;
			case LEVEL:
				p1.addExtra("等級需求： " + q.getRequirements().get(t).toString() + " ");
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&7[編輯]", "/mq e edit req LEVEL"));
				p1.addExtra("\n");
				break;
			case MONEY:
				p1.addExtra("金錢需求： " + q.getRequirements().get(t).toString() + " ");
				p1.addExtra(TextComponentFactory.regClickCmdEvent("&7[編輯]", "/mq e edit req MONEY"));
				p1.addExtra("\n");
				break;
				
			case NBTTAG:
				p2.addExtra("NBT標籤需求：");
				p2.addExtra("\n");
				i = 0;
				for (String s : (List<String>)q.getRequirements().get(t)){
					p2.addExtra("- ");
					p2.addExtra(s);
					p2.addExtra(TextComponentFactory.regClickCmdEvent("&7[編輯]", "/mq e edit req NBTTAG " + i));
					p2.addExtra(TextComponentFactory.regClickCmdEvent("&7[移除]", "/mq e remove req NBTTAG " + i));
					p2.addExtra("\n");
					i++;
				}
				p2.addExtra(TextComponentFactory.regClickCmdEvent("&0&l[新增]", "/mq e addnew req NBTTAG " + i));
				p2.addExtra("\n");
				break;
			case QUEST:
				p2.addExtra("任務需求：");
				p2.addExtra("\n");
				i = 0;
				for (String s : (List<String>)q.getRequirements().get(t)){
					p2.addExtra("- ");
					p2.addExtra(s);
					p2.addExtra(TextComponentFactory.regClickCmdEvent("&7[編輯]", "/mq e edit req QUEST " + i));
					p2.addExtra(TextComponentFactory.regClickCmdEvent("&7[移除]", "/mq e remove req QUEST " + i));
					p2.addExtra("\n");
					i++;
				}
				p2.addExtra(TextComponentFactory.regClickCmdEvent("&0&l[新增]", "/mq e addnew req QUEST " + i));
				p2.addExtra("\n");
				break;
			case SCOREBOARD:
				p2.addExtra("記分板需求：");
				p2.addExtra("\n");
				i = 0;
				for (String s : (List<String>)q.getRequirements().get(t)){
					p2.addExtra("- ");
					p2.addExtra(s);
					p2.addExtra(TextComponentFactory.regClickCmdEvent("&7[編輯]", "/mq e edit req SCOREBOARD " + i));
					p2.addExtra(TextComponentFactory.regClickCmdEvent("&7[移除]", "/mq e remove req SCOREBOARD " + i));
					p2.addExtra("\n");
					i++;
				}
				p2.addExtra(TextComponentFactory.regClickCmdEvent("&0&l[新增]", "/mq e addnew req SCOREBOARD " + i));
				p2.addExtra("\n");
				break;
			}
		}
		p1.addExtra("\n");
		p1.addExtra(TextComponentFactory.regClickCmdEvent("&0&l[返回任務選單]", "/mq e gui"));
		QuestGUIManager.openBook(p, p1, p2);
	}
	
	public static void selectTriggerType(Player p){
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l選擇觸發事件時機： \n"));
		for (TriggerType t : TriggerType.values()){
			p1.addExtra(TextComponentFactory.regClickCmdEvent("- [" + t.toCustomString() + "]", "/mq e addnew evt " + t.toString()));
			p1.addExtra("\n");
		}
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void selectStage(Player p, TriggerType t){
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l選擇觸發事件階段： \n"));
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		for (int s = 1; s <= q.getStages().size(); s++){
			p1.addExtra(TextComponentFactory.regClickCmdEvent("- [階段" + s + "]", "/mq e addnew evt " + t.toString() + " " + s));
			p1.addExtra("\n");
		}
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void selectTriggerObject(Player p, TriggerType t, int s){
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l選擇觸發物件： \n"));
		for (TriggerObject o : TriggerObject.values()){
			if (t.equals(TriggerType.TRIGGER_STAGE_START) || t.equals(TriggerType.TRIGGER_STAGE_FINISH)){
				p1.addExtra(TextComponentFactory.regClickCmdEvent("- [" + o.toCustomString() + "]", "/mq e addnew evt " + t.toString() + " " + s + " " + o.toString()));
				p1.addExtra("\n");
				continue;
			}
			p1.addExtra(TextComponentFactory.regClickCmdEvent("- [" + o.toCustomString() + "]", "/mq e addnew evt " + t.toString() + " " + o.toString()));
			p1.addExtra("\n");
		}
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void selectObjectType(Player p, int stage, int obj){
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l選擇任務目標類別： \n"));
		for (String s : SimpleQuestObject.ALL_OBJECTS.keySet()){
			p1.addExtra(TextComponentFactory.regClickCmdEvent("- [" + SimpleQuestObject.ALL_OBJECTS.get(s) + "]", "/mq e edit object " + stage + " " + obj + " type " + s));
			p1.addExtra("\n");
		}
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void createQuest(Player p){
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&9&l新建任務》"));
		p1.addExtra("\n");
		p1.addExtra(QuestUtil.translateColor("&0若要新建任務，\n確認以下兩個&c&l必須&0的參數輸入完成後，\n&0點擊&a&l創建&0按鈕，\n即可創建新任務。"));
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		p1.addExtra("\n");
		if (q.getInternalID() == null)
			p1.addExtra(QuestUtil.translateColor("&0新建任務內部ID： &c未設定"));
		else
			p1.addExtra(QuestUtil.translateColor("&0新建任務內部ID： " + q.getInternalID()));
		p1.addExtra("\n");
		p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e newquest id"));
		p1.addExtra("\n");
		if (q.getQuestName() == null)
			p1.addExtra(QuestUtil.translateColor("&0新建任務顯示名稱： &c未設定"));
		else
			p1.addExtra(QuestUtil.translateColor("&0新建任務顯示名稱： " + q.getQuestName()));
		p1.addExtra("\n");
		p1.addExtra(TextComponentFactory.regClickCmdEvent("&0 &l[編輯]", "/mq e newquest name"));
		p1.addExtra("\n\n");
		p1.addExtra(TextComponentFactory.regClickCmdEvent("&2       &l【創建任務】", "/mq e newquest create"));
		QuestGUIManager.openBook(p, p1);
	}
	
	public static void generateEditItemGUI(Player p, String type, List<ItemStack> list){
		if (QuestEditorManager.isInEditorMode(p)){
			Quest q = QuestEditorManager.getCurrentEditingQuest(p);
			Inventory inv = Bukkit.createInventory(null, 27, "《" + type + "》" + q.getQuestName());
			inv.addItem(list.toArray(new ItemStack[list.size()]));
			p.openInventory(inv);
		}
		else return;
	}
}
