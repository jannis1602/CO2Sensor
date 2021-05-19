package pack;

import java.awt.GridLayout;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Server {
	private static boolean running = true;
	private boolean saveData = true;
	public static String loadOtherDate = null;// format: 2021-02-06
	private GraphPanel graphCo2;
	private GraphPanel graphTemp;
	private long nextSave;
	private final int intervall = 2; // 5
	LinkedList<Sensor> sensoren;
	public static boolean oldSave = false;

	public static void main(String[] args) {
		if (args.length > 0) {
			loadOtherDate = args[0];
			running = false;
			oldSave = true;
		}
//		running = false;
//		loadOtherDate = "2021-02-15";
//		oldSave = true;
		new Server();
	}

	public Server() {
		nextSave = LocalDateTime.now().getMinute() + intervall - LocalDateTime.now().getMinute() % intervall;
		if (nextSave >= 60)
			nextSave = nextSave - 60;
		if (!oldSave)
			System.out.println("save: " + LocalDateTime.now().getMinute() + " ---> nextSave: " + nextSave);
		String[] temp = readConfig();

		String[] sensorIPs = new String[temp.length];
		String[] sensorNamen = new String[temp.length];
		for (int i = 0; i < temp.length; i++) {
			sensorIPs[i] = temp[i].split("#")[0];
			sensorNamen[i] = temp[i].split("#")[1];
		}

		sensoren = new LinkedList<Sensor>();
		for (int i = 0; i < sensorIPs.length; i++) {
			sensoren.add(new Sensor(sensorIPs[i]));
			if (!oldSave)
				sensoren.getLast().loadSaves();
			else
				sensoren.getLast().loadOldSaves(loadOtherDate);
		}
		JFrame frame = new JFrame("Server");
		frame.setBounds(1000, 1000, 1000, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(2, 1));
		List<List<Integer>> tempCO2List = new LinkedList<>();
		for (int i = 0; i < sensoren.size(); i++) {
			tempCO2List.add(sensoren.get(i).getCO2List());
		}
		graphCo2 = new GraphPanel(tempCO2List, 0, 5000, 20); // 5000 or 3000
		graphCo2.setSensorNamen(sensorNamen);
		frame.add(graphCo2);
		List<List<Integer>> tempTemperaturList = new LinkedList<>();
		for (int i = 0; i < sensoren.size(); i++) {
			tempTemperaturList.add(sensoren.get(i).getTemperaturList());
		}
		graphTemp = new GraphPanel(tempTemperaturList, 0, 34, 12);
		graphTemp.rendercolorname = false;
		frame.add(graphTemp);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		if (oldSave) {
			graphCo2.repaint();
			graphTemp.repaint();
		}
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					try {
						if (LocalDateTime.now().getHour() == 0 && LocalDateTime.now().getMinute() < 2)
							for (Sensor tempSensor : sensoren) {
								tempSensor.setWerteNull();
							}
						for (Sensor tempSensor : sensoren) {
							tempSensor.updateMesswerte();
						}
						Thread.sleep(22000);// min 22sec wegen Timeout

						long minutes = LocalDateTime.now().getMinute();
						if (minutes >= nextSave && minutes - intervall - 1 < nextSave) {
							nextSave = minutes + intervall;
							if (nextSave >= 60)
								nextSave = nextSave - 60;
							System.out.println("save: " + minutes + " ---> nextSave: " + nextSave);
							if (saveData == true)
								for (Sensor tempSensor : sensoren) {
									tempSensor.save();
								}
						}
						graphCo2.repaint();
						graphTemp.repaint();
					} catch (InterruptedException e) {
					}
				}
			}
		});
		thread.start();

	}

	public void update() {
		for (int i = 0; i <= 3; i++)
			addTemperaturFromWebsiteToGraph("http://"+sensoren.get(i).getIP()+ "/sensor", i);
	}

	private void addTemperaturFromWebsiteToGraph(String webside, int sensor) {
		try {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					String temp = getWebsiteText(webside)[1];
					if (temp == null) {
						System.out.println("ERROR: " + sensor);
						return;
					}
					String[] wert = temp.split(":");
					System.out.println(wert[1]);
//					graph.updateGraph();
				}
			});
			thread.start();
		} catch (Exception e) {
			System.out.println("ERROR: " + sensor);
		}
	}

	private String[] getWebsiteText(String webseite) {
		String[] text = new String[2];
		try {
			URL url = new URL(webseite);
			Scanner scanner = new Scanner(url.openStream()); // ca.20sec timeout
			StringBuffer sBuffer = new StringBuffer();
			while (scanner.hasNext())
				sBuffer.append(scanner.next());
			String result = sBuffer.toString();
			result = result.replaceAll("<[^>]*>", "");
			String[] temp = result.split("#");
			text[0] = temp[1];
			text[1] = temp[2];
		} catch (IOException e) {
			return null;
		}
		return text;
	}

	private String[] readConfig() {
		String[] temp = null;
		try {
			temp = ladeDatei(getJarExecutionDirectory() + "config.txt").split("\n");
		} catch (Exception e) {
			e.printStackTrace();
			int result = JOptionPane.showConfirmDialog(null, getJarExecutionDirectory() + " Error! config.txt",
					"Achtung!", JOptionPane.OK_CANCEL_OPTION);
			if (result != JOptionPane.OK_OPTION || result == JOptionPane.OK_OPTION)
				System.exit(0);
		}
		for (int i = 0; i < temp.length; i++) {
			temp[i] = temp[i].replaceAll("\\r\\n|\\r|\\n", "");
			System.out.println(temp[i]);
		}
		return temp;
	}

	private static String ladeDatei(String datName) { // replace with class
		File file = new File(datName);
		System.out.println("configDatei: " + datName);
		if (!file.canRead() || !file.isFile())
			return null;
		FileReader fr = null;
		int c;
		StringBuffer buff = new StringBuffer();
		try {
			fr = new FileReader(file);
			while ((c = fr.read()) != -1)
				buff.append((char) c);
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buff.toString();
	}

	public static String getJarExecutionDirectory() {
		File f = new File(System.getProperty("java.class.path"));
		File dir = f.getAbsoluteFile().getParentFile();
		String path = dir.toString() + System.getProperty("file.separator");
		return path;
	}

}