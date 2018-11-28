package asylum.nursebot.modules;

import java.util.Calendar;
import java.util.List;

import com.google.inject.Inject;
import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.LineStyle;
import com.googlecode.charts4j.LinearGradientFill;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Shape;

import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Module;
import asylum.nursebot.objects.ModuleType;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.objects.Visibility;
import asylum.nursebot.persistence.ModelManager;
import asylum.nursebot.persistence.modules.StatisticsMessage;
import asylum.nursebot.semantics.SemanticInterpreter;
import asylum.nursebot.semantics.SemanticsHandler;
import asylum.nursebot.semantics.WakeWord;
import asylum.nursebot.semantics.WakeWordType;

@AutoModule(load=true)
public class Statistics implements Module {

	@Inject
	private CommandHandler commandHandler;
	@Inject
	private SemanticsHandler semanticsHandler;
	
	private CommandCategory category;

	@Override
	public String getName() {
		return "Statistics";
	}

	@Override
	public ModuleType getType() {
		return new ModuleType()
				.set(ModuleType.COMMAND_MODULE)
				.set(ModuleType.SEMANTIC_MODULE);
	}

	public Statistics() {
		ModelManager.build(StatisticsMessage.class);
		
		this.category = new CommandCategory("Statistiken");
	}
	
	
	public String graphMessagesWeek(long chatid) {
		List<StatisticsMessage> list = StatisticsMessage.where("chatid = ?", chatid);
		
		int[] days = new int[]{0, 0, 0, 0, 0, 0, 0};
		int[] nrweeks = new int[]{0, 0, 0, 0, 0, 0, 0};
		
		int last = -1;
		for (StatisticsMessage message : list) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(message.getTime());
			int day = calendar.get(Calendar.DAY_OF_WEEK) - 2;
			if (day < 0) // because of sunday
				day = 6;
			days[day]++;
			int current = day * 100 + calendar.get(Calendar.WEEK_OF_YEAR);
			if (last != current) {
				last = current;
				nrweeks[day]++;
			}
		}
		
		double[] perday = new double[]{0, 0, 0, 0, 0, 0, 0};
		
		for(int i = 0; i < days.length; i++) {
			if (nrweeks[i] == 0)
				continue;
			
			perday[i] = days[i] / nrweeks[i];
		}
		
		return generateLineGraph(perday, "Nachrichten pro Wochentag", new String[]{"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"});
	}
	
	public String generateLineGraph(double data[], String title, String xLabels[]) {
		Color messageColor = Color.newColor("CA3D05");
		//Color wordColor = Color.newColor("6666FF");
		
		int max = -1;
		
		for(int i = 0; i < data.length; i++) {
			if (data[i] > max)
				max = (int) Math.ceil(data[i]);
		}
		
		int magnitude = 10;
		
		while (max / magnitude > magnitude)
			magnitude *= 10;
		
		if (max % magnitude != 0) 
			max = (max / magnitude + 1) * magnitude;
		
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i] / max * 100;
		}
		
		Line messageLine = Plots.newLine(Data.newData(data), messageColor);
		messageLine.setLineStyle(LineStyle.newLineStyle(3, 1, 0));
		messageLine.addShapeMarkers(Shape.DIAMOND, messageColor, 12);
		messageLine.addShapeMarkers(Shape.DIAMOND, Color.WHITE, 8);
		
		LineChart chart = GCharts.newLineChart(messageLine);
		
		chart.setSize(600, 450);
		chart.setTitle(title, Color.WHITE, 14);
		//chart.addHorizontalRangeMarker(40, 60, Color.newColor(Color.RED, 30));
        //chart.addVerticalRangeMarker(70, 90, Color.newColor(Color.GREEN, 30));
		
		double xgrid = 100 / (double) (xLabels.length - 1);
		while (xgrid < 10)
			xgrid *= 2;
		
		int numberYSteps = 10;
		
        chart.setGrid(xgrid, 100 / numberYSteps, 3, 2);

        AxisStyle axisStyle = AxisStyle.newAxisStyle(Color.WHITE, 12, AxisTextAlignment.CENTER);
        AxisLabels xAxis = AxisLabelsFactory.newAxisLabels(xLabels);
        xAxis.setAxisStyle(axisStyle);
        
        String[] yLabels = new String[numberYSteps + 1];
        
        for(int i = 0; i < numberYSteps + 1; i++) {
        	yLabels[i] = String.valueOf(Math.round(max / (double) numberYSteps * i * 10) / 10);
        }
        
        AxisLabels yAxis = AxisLabelsFactory.newAxisLabels(yLabels);
        yAxis.setAxisStyle(axisStyle);
        
        chart.addXAxisLabels(xAxis);
        chart.addYAxisLabels(yAxis);
        
        chart.setBackgroundFill(Fills.newSolidFill(Color.newColor("1F1D1D")));
        LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.newColor("363433"), 100);
        fill.addColorAndOffset(Color.newColor("2E2B2A"), 0);
        chart.setAreaFill(fill);
        
        return chart.toURLString();
	}
	
	@Override
	public void init() {
		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord(null, WakeWordType.TEXT_MESSAGE))
				.setLocality(Locality.GROUPS)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					new StatisticsMessage()
						.setChatId(c.getMessage().getChatId())
						.setLength(c.getMessage().getText().split(" ").length)
						.saveIt();
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("statsweek")
				.setInfo("zeigt die Verteilung der Nachrichten über die Woche an")
				.setLocality(Locality.GROUPS)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setCategory(category)
				.setAction(c -> {
					String link = graphMessagesWeek(c.getMessage().getChatId());
					c.getSender().send(link);
				}));
	}

	@Override
	public void activate() {
		
	}

	@Override
	public void deactivate() {
		
	}

	@Override
	public void shutdown() {
		
	}

}
