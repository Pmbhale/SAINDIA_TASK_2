package Wheather;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.Locale;
import org.json.*;

public class WeatherForecast {
    JFrame frame;
    JTextField cityField;
    JButton searchButton;
    JLabel smileLabel, backgroundLabel;
    JPanel forecastPanel;

    public WeatherForecast() {
        frame = new JFrame("5-Day Weather Forecast");
        frame.setSize(1300, 700);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Background image
        backgroundLabel = new JLabel(new ImageIcon(new ImageIcon("Back.png")
                .getImage().getScaledInstance(1300, 700, Image.SCALE_SMOOTH)));
        backgroundLabel.setBounds(0, 0, 1300, 700);
        frame.add(backgroundLabel);

        // Greeting
        JLabel greeting = new JLabel(getGreeting(), SwingConstants.CENTER);
        greeting.setFont(new Font("SansSerif", Font.BOLD, 60));
        greeting.setForeground(new Color(69, 137, 179));
        greeting.setBounds(300, 10, 600, 70);
        backgroundLabel.add(greeting);

        // Input Panel
        JPanel inputPanel = new JPanel(null);
        inputPanel.setBounds(370, 100, 450, 60);
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(46, 134, 193), 2));
        backgroundLabel.add(inputPanel);

        cityField = new JTextField();
        cityField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        cityField.setBounds(10, 15, 300, 30);
        inputPanel.add(cityField);

        ImageIcon searchIcon = new ImageIcon(new ImageIcon("Search.png")
                .getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        searchButton = new JButton(searchIcon);
        searchButton.setBounds(320, 15, 40, 30);
        inputPanel.add(searchButton);

        ImageIcon smileIcon = new ImageIcon(new ImageIcon("Smile.png")
                .getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        smileLabel = new JLabel(smileIcon);
        smileLabel.setBounds(370, 15, 50, 30);
        inputPanel.add(smileLabel);

        // Forecast panel
        forecastPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        forecastPanel.setBounds(50, 200, 1200, 350);
        forecastPanel.setOpaque(false);
        backgroundLabel.add(forecastPanel);

        // Add default empty cards


        // Search button logic
        searchButton.addActionListener(e -> {
            String city = cityField.getText().trim();
            if (city.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a city name.");
            } else {
                for (int i = 0; i < 5; i++) {
                    forecastPanel.add(createForecastCard("Day", "Date", "0°C", "Clear", "0%", "0 km/h"));
                }
                fetchWeatherData(city);
            }
        });

        frame.setVisible(true);
    }

    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "🌅 Good Morning!";
        else if (hour < 18) return "🌞 Good Afternoon!";
        else return "🌙 Good Evening!";
    }

    private void fetchWeatherData(String city) {
        try {
            String KEY = "4d51167f6a9deb11b5231060be04ee90"; // Your real API key
            String urlStr = "https://api.openweathermap.org/data/2.5/forecast?q="
                    + URLEncoder.encode(city, "UTF-8") + "&appid=" + KEY + "&units=metric";
            HttpURLConnection con = (HttpURLConnection) new URL(urlStr).openConnection();
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder(); String l;
            while ((l = r.readLine()) != null) sb.append(l);
            r.close();

            JSONObject resp = new JSONObject(sb.toString());
            JSONArray list = resp.getJSONArray("list");

            List<JSONObject> arr = new ArrayList<>();
            for (int i = 0; i < list.length(); i++) {
                String t = list.getJSONObject(i).getString("dt_txt");
                if (t.contains("12:00:00")) arr.add(list.getJSONObject(i));
                if (arr.size() == 5) break;
            }

            forecastPanel.removeAll();
            for (JSONObject obj : arr) {
                JSONObject m = obj.getJSONObject("main");
                JSONObject w = obj.getJSONArray("weather").getJSONObject(0);
                JSONObject wind = obj.getJSONObject("wind");

                String te = (int)m.getDouble("temp") + "°C";
                String co = w.getString("main");
                String hu = m.getInt("humidity") + "%";
                String wi = (int)wind.getDouble("speed") + " km/h";

                LocalDate d = LocalDate.parse(obj.getString("dt_txt").split(" ")[0]);
                String dn = d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                String ds = d.getDayOfMonth() + " " + d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

                forecastPanel.add(createForecastCard(dn, ds, te, co, hu, wi));
            }

            forecastPanel.revalidate();
            forecastPanel.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private JPanel createForecastCard(String day, String date, String temp, String cond, String humidity, String wind) {
        String iconFile = "clear.png"; // default

        if (cond.equalsIgnoreCase("Rain")) iconFile = "rain.png";
        else if (cond.equalsIgnoreCase("Clouds")) iconFile = "cloudy.png";
        else if (cond.equalsIgnoreCase("Clear")) iconFile = "clear.png";

        JPanel card = new JPanel();
        card.setLayout(null);
        card.setPreferredSize(new Dimension(200, 300));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true));

        JLabel dateLabel = new JLabel("<html><center>" + day + "<br>" + date + "</center></html>", SwingConstants.CENTER);
        dateLabel.setBounds(40, 5, 120, 40);
        card.add(dateLabel);

        try {
            ImageIcon weatherIcon = new ImageIcon(new ImageIcon(getClass().getResource(iconFile))
                    .getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            JLabel ico = new JLabel(weatherIcon);
            ico.setBounds(60, 50, 100, 100);
            card.add(ico);
        } catch (Exception e) {
            System.err.println("Missing image: " + iconFile);
        }

        JLabel tempLabel = new JLabel(temp, SwingConstants.CENTER);
        tempLabel.setBounds(60, 120, 100, 100);
        tempLabel.setFont(new Font("Dialog", Font.BOLD, 28));
        card.add(tempLabel);

        JLabel condLabel = new JLabel(cond, SwingConstants.CENTER);
        condLabel.setBounds(60, 160, 100, 100);
        condLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        card.add(condLabel);

        JLabel huIcon = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource("humidity.png"))
                .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        huIcon.setBounds(10, 270, 50, 50);
        card.add(huIcon);

        JLabel huText = new JLabel("<html><b>Humidity</b><br>" + humidity + "</html>");
        huText.setBounds(55, 270, 75, 50);
        card.add(huText);

        JLabel windIcon = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource("windspeed.png"))
                .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        windIcon.setBounds(110, 270, 50, 50);
        card.add(windIcon);

        JLabel windText = new JLabel("<html><b>Windspeed</b><br>" + wind + "</html>");
        windText.setBounds(165, 270, 75, 50);
        card.add(windText);

        return card;
    }

    public static void main(String[] args) {
        new WeatherForecast();
    }
}
