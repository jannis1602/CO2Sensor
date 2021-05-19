package pack;

import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class GraphPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private int padding = 25;
	private int labelPadding = 25;
//	private Color lineColor = new Color(44, 102, 230, 180);
	private Color pointColor = new Color(100, 100, 100, 180);
	private Color gridColor = new Color(200, 200, 200, 200);
	private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
	private int pointWidth = 4;
	private int numberYDivisions = 20;
	private int numberXDivisions = 25;
	private int Ymin = 0;
	private int Ymax = 0;
	private int maxYZahlen = 20;
	boolean rendercolorname = true;
	boolean backgroundColor = false;
	List<List<Integer>> werteList;
	Color[] colorList = { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.CYAN }; // color list als
																								// parameter?
	private String[] sensorNamen = { "room1", "room2", "room3", "room4" };
//	Color[] colorList = { new Color(207, 31, 198), new Color(198, 198, 3), new Color(0, 120, 163), new Color(0, 132, 0)};

	/**
	 * @param werteList  Liste mit allen wertelisten
	 * @param Ymin       Y-Achse minimum
	 * @param Ymax       Y-Achse maximum
	 * @param maxYZahlen Anzahl den Zahlen an der Y-Achse
	 */
	public GraphPanel(List<List<Integer>> werteList, int Ymin, int Ymax, int maxYZahlen) {
		this.werteList = werteList;
		this.Ymin = Ymin;
		this.Ymax = Ymax;
		this.maxYZahlen = maxYZahlen;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		int listsize = 288;// werteList.get(0).size();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (listsize - 1);
		double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

//PUNKTE festlegen
		List<List<Point>> graphPointsList = new ArrayList<>();
		for (List<Integer> scores : werteList) {
			List<Point> graphPoints = new ArrayList<>();
			for (int i = 0; i < listsize; i++) {
				int x1 = (int) (i * xScale + padding + labelPadding);
				int y1 = (int) ((getMaxScore() - scores.get(i)) * yScale + padding);
				graphPoints.add(new Point(x1, y1));
			}
			graphPointsList.add(graphPoints);
		}

		// draw white background
		g2d.setColor(Color.WHITE);
		g2d.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding,
				getHeight() - 2 * padding - labelPadding);
		g2d.setColor(Color.BLACK);

		// grün gelb rot Background
		if (backgroundColor) {
			if (Ymax > 100) {
				g2d.setColor(new Color(176, 4, 4, 40));
				g2d.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding,
						getHeight() - 2 * padding - labelPadding - (int) (2000 * yScale));
				g2d.setColor(new Color(217, 224, 13, 40));

				g2d.fillRect(padding + labelPadding, padding + (int) (3000 * yScale),
						getWidth() - (2 * padding) - labelPadding,
						getHeight() - 2 * padding - labelPadding - (int) (3000 * yScale));
				g2d.setColor(new Color(0, 153, 5, 40));
				g2d.fillRect(padding + labelPadding, padding + (int) (4000 * yScale),
						getWidth() - (2 * padding) - labelPadding,
						getHeight() - 2 * padding - labelPadding - (int) (4000 * yScale));
			}
		}
		g2d.setColor(Color.BLACK);

		numberYDivisions = (int) (getMaxScore() - getMinScore());
		int teiler = numberYDivisions / maxYZahlen;
		if (teiler == 0)
			teiler = 1;
		for (int i = 0; i < numberYDivisions + 1; i++) {
			if (i % teiler == 0) {
				int x0 = padding + labelPadding;
				int x1 = pointWidth + padding + labelPadding;
				int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding
						+ labelPadding);
				int y1 = y0;
				if (listsize > 0) {
					g2d.setColor(gridColor);
					if (i % 8 == 0) {
						g2d.setStroke(new BasicStroke(2));
					}

					g2d.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
					g2d.setColor(Color.BLACK);
					g2d.setStroke(new BasicStroke(1));
					String yLabel = ((int) ((getMinScore()
							+ (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)))) + "";
					FontMetrics metrics = g2d.getFontMetrics();
					int labelWidth = metrics.stringWidth(yLabel);
					g2d.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
				}
				g2d.drawLine(x0, y0, x1, y1);
			}
		}

		// and for x axis
		if (listsize > 288)
			System.out.println("Error: " + listsize + " > " + 288);
		for (int i = 0; i < listsize; i++) {
			if (listsize > 1) {
				int x0 = i * (getWidth() - padding * 2 - labelPadding) / (listsize - 1) + padding + labelPadding;
				int x1 = x0;
				int y0 = getHeight() - padding - labelPadding;
				int y1 = y0 - pointWidth;
				if ((i % ((int) ((listsize / numberXDivisions)) + 1)) == 0) {// 20.0
					g2d.setColor(gridColor);
					g2d.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
					g2d.setColor(Color.BLACK);
					String xLabel = i / (60 / 5) + "";
					FontMetrics metrics = g2d.getFontMetrics();
					int labelWidth = metrics.stringWidth(xLabel);
					g2d.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
				}
				g2d.drawLine(x0, y0, x1, y1);
			}
		}
		if (Ymax < 100)
			g.drawString("Temperatur[°C]", padding + 25, padding - 4);
		else
			g.drawString("CO2-Konz.[PPM]", padding + 25, padding - 4);
		g.drawString("Zeit[h]", getWidth() - padding - 16, getHeight() - 32); // g.getFontMetrics().stringWidth("Zeit[h]")

		// create x and y axes
		g2d.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
		g2d.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding,
				getHeight() - padding - labelPadding);

// TODO Point liste hier erstellen

//LINIEN
		for (int iGraph = 0; iGraph < graphPointsList.size(); iGraph++) {
			List<Point> graphPoints = graphPointsList.get(iGraph);
			Stroke oldStroke = g2d.getStroke();
			g2d.setColor(colorList[iGraph]);
			g2d.setStroke(GRAPH_STROKE);
			for (int i = 0; i < graphPoints.size() - 1; i++) {
				if (Server.oldSave
						|| i < LocalDateTime.now().getHour() * 12 + (LocalDateTime.now().getMinute() - 1) / 5) {
					int x1 = graphPoints.get(i).x;
					int y1 = graphPoints.get(i).y;
					int x2 = graphPoints.get(i + 1).x;
					int y2 = graphPoints.get(i + 1).y;
					g2d.drawLine(x1, y1, x2, y2);
				}
			}
//PUNKTE
			g2d.setStroke(oldStroke);
			g2d.setColor(pointColor);
			for (int i = 0; i < graphPoints.size(); i++) {
				if (Server.oldSave
						|| i - 1 < LocalDateTime.now().getHour() * 12 + (LocalDateTime.now().getMinute() - 1) / 5) {
					int x = graphPoints.get(i).x - pointWidth / 2;
					int y = graphPoints.get(i).y - pointWidth / 2;
					int ovalW = pointWidth;
					int ovalH = pointWidth;
					g2d.fillOval(x, y, ovalW, ovalH);
				}
			}
		}
//render Sensornamen
		if (rendercolorname) {
			int namesWidth = 0;
			for (int i = 0; i < werteList.size(); i++) {
				namesWidth += 30;
				namesWidth += g.getFontMetrics().stringWidth(sensorNamen[i]) + 10;
			}
			int posx = (getWidth() - namesWidth) / 2;
			for (int i = 0; i < werteList.size(); i++) {
				g.setColor(colorList[i]);
				g.fillRect(posx, getHeight() - 24, 24, 24);
				posx += 30;
				g.setFont(new Font("Serif", Font.PLAIN, 24));
				g.setColor(Color.BLACK);
				g.drawString(sensorNamen[i], posx, getHeight() - 4);
				posx += g.getFontMetrics().stringWidth(sensorNamen[i]) + 10;
			}
		}

	}

	private double getMinScore() {
		return Ymin;
	}

	private double getMaxScore() {
		return Ymax;
	}

	public void setSensorNamen(String[] namen) {
		sensorNamen = namen;
	}
}