package pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;

import text.FileWriter;
import text.MyFileReader;

public class Sensor {
	private String ip;
//	private int maxWerte = 1000; // max list laenge // 60/5*24=288 // 60/2*24=720
	private boolean error = false;// TODO
	private LinkedList<Integer> temperaturWerte;
	private LinkedList<Integer> co2Werte;

	public Sensor(String ip) { // sensorName
		this.ip = ip;
		temperaturWerte = new LinkedList<Integer>();
		co2Werte = new LinkedList<Integer>();
		for (int i = 0; i < 288; i++) // 288 alle 5 min
			temperaturWerte.add(0);
		for (int i = 0; i < 288; i++)
			co2Werte.add(0);
	}

	public String getIP() {
		return ip;
	}
	
	public void updateMesswerte() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				String[] webWerte = new String[3];
				String res = getWebsideContent(ip);
				if (res != null) {
					String[] temp = res.split("#");
					webWerte[0] = temp[1].replace(" ", ""); // humidity
					webWerte[1] = temp[2].replace(" ", ""); // temperatur
					webWerte[2] = temp[3].replace(" ", ""); // co2
					error = false;
				} else {
					System.out.println(ip + " nicht erreichbar!");
					webWerte = new String[] { "0:0", "0:0", "0:0" };
					error = true;
				}
// Temperaturwerte
				String temp = webWerte[1];
				if (temp == null) {
					System.out.println("ERROR: " + ip);
					return;
				}
				String[] wert = temp.split(":");
				String time = new SimpleDateFormat("HH:mm").format(new Date());
				System.out.print(time + " - " + ip + " - temperatur: " + wert[1]);
				if (Integer.valueOf(wert[1]) != 0)
					temperaturWerte.set(LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5,
							Integer.valueOf(wert[1]));
// CO2Werte
				temp = webWerte[2];
				if (temp == null) {
					System.out.println("\n ERROR: " + ip);
					return;
				}
				wert = temp.split(":");
				System.out.println(" - CO2: " + wert[1]); // TODO getWert at and save wert
				if (Integer.valueOf(wert[1]) != 0)
					co2Werte.set(LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5,
							Integer.valueOf(wert[1]));
			}
		});
		thread.start();
	}

	private String getWebsideContent(String website) {
		website = "http://" + website;
		String parseLine = null;
		String result = null;
		try {
			URL URL = new URL(website);
			BufferedReader br = new BufferedReader(new InputStreamReader(URL.openStream()));

			while ((parseLine = br.readLine()) != null) {
				result += parseLine;
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void save() {
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String time = new SimpleDateFormat("HH:mm").format(new Date());
		if (!error) {
			new FileWriter(Server.getJarExecutionDirectory() + "files" + File.separator + ip + File.separator,
					date + "--" + ip + ".txt", time + "#" + getLastTemprature() + "#" + getLastCo2());
			System.out.println(
					"Save: " + Server.getJarExecutionDirectory() + "files" + File.separator + ip + File.separator + date
							+ "--" + ip + ".txt" + " ->: " + time + "#" + getLastTemprature() + "#" + getLastCo2());
		}
	}

	public void loadSaves() {
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		if (Server.loadOtherDate != null)
			date = Server.loadOtherDate;
		String fileName = date + "--" + ip + ".txt";
		File saveFile = new File(
				Server.getJarExecutionDirectory() + "files" + File.separator + ip + File.separator + fileName);
		if (saveFile.exists()) {
			String[] temp = new MyFileReader().readFile(saveFile).split("\n");
			for (String s : temp) {
				String[] werte = s.split("#");
				String[] time = werte[0].split(":");
				int listPos = 0;
				listPos = Integer.valueOf(time[0]) * 12 + Integer.valueOf(time[1]) / 5;
				werte[2] = werte[2].replaceAll("[\\D]", "");
				temperaturWerte.set(listPos, Integer.valueOf(werte[1]));
				co2Werte.set(listPos, Integer.valueOf(werte[2]));
			}
		}
	}

	public void loadOldSaves(String date) {
		if (Server.loadOtherDate != null)
			date = Server.loadOtherDate;
		String fileName = date + "--" + ip + ".txt";
		File saveFile = new File(
				Server.getJarExecutionDirectory() + "files" + File.separator + ip + File.separator + fileName);
		if (saveFile.exists()) {
			String[] temp = new MyFileReader().readFile(saveFile).split("\n");
			for (String s : temp) {
				String[] werte = s.split("#");
				String[] time = werte[0].split(":");
				int listPos = 0;
				listPos = Integer.valueOf(time[0]) * 12 + Integer.valueOf(time[1]) / 5;
				werte[2] = werte[2].replaceAll("[\\D]", "");
				temperaturWerte.set(listPos, Integer.valueOf(werte[1]));
				co2Werte.set(listPos, Integer.valueOf(werte[2]));
			}
		}
	}

	private int getLastTemprature() { // könnte null sein, co2 nicht
		if (temperaturWerte.get(LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5) == 0)
			if (LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5 != 0)
				return temperaturWerte
						.get(LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5 - 1);
			else
				return temperaturWerte.getLast();
		return temperaturWerte.get(LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5);
	}

	private int getLastCo2() {
		if (co2Werte.get(LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5) == 0)
			if (LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5 != 0)
				return co2Werte.get(LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5 - 1);
			else
				return co2Werte.getLast();
		return co2Werte.get(LocalDateTime.now().getHour() * 12 + LocalDateTime.now().getMinute() / 5);
	}

	public LinkedList<Integer> getTemperaturList() {
		return temperaturWerte;
	}

	public LinkedList<Integer> getCO2List() {
		return co2Werte;
	}

	public void setWerteNull() {
		for (int i = 0; i < co2Werte.size(); i++) {
			co2Werte.set(i, 0);
			temperaturWerte.set(i, 0);
		}
	}
}
