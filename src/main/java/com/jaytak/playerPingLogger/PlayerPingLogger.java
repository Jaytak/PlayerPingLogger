package com.jaytak.playerPingLogger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlayerPingLogger extends JavaPlugin implements Listener {
    private File historyFile;
    private final List<String> players = new ArrayList<>();
    private Map<String, Integer> playerTasks = new HashMap<>();


    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        historyFile = new File(getDataFolder(), "playerPingHistory.txt");
        if (!historyFile.exists()) {
            try{
                getDataFolder().mkdirs();
                historyFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Failed to create history file.");
                throw new RuntimeException(e);
            }
        }
        int pluginId = 23544;
        Metrics metrics = new Metrics(this, pluginId);
        getLogger().info("PlayerPingLogger is enabled");
        JTLogger("PlayerPingLogger is enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PlayerPingLogger is disabled");
        JTLogger("PlayerPingLogger is disabled");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        JTLogger("Player " + playerName  + " joined.");

        if (players.contains(playerName)) {
            JTLogger("Task already running for player " + playerName);
            return;
        } else {
            players.add(playerName);
            JTLogger("Creating new task for player " + playerName);
        }

        int taskId = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    JTLogger("Player " + playerName + "'s ping: " + player.getPing() + "ms");
                } else {
                    JTLogger("Player " + playerName + " disconnected before capturing further ping measurements.");
                    players.remove(playerName);
                    Bukkit.getScheduler().cancelTask(playerTasks.get(playerName));
                    playerTasks.remove(playerName);
                }
            }
        }, 0, 20 * 60).getTaskId();

        playerTasks.put(playerName, taskId);
    }


    private void JTLogger(String log){
        try(FileWriter writer = new FileWriter(historyFile, true)){
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:hh:mm:ss"));
            writer.write("[" + timestamp + "] " + log + "\n");
        }
        catch (Exception e){
            getLogger().severe("Player Ping Logger. Failed to log changes. Exception:\n" + e);
        }
    }
}
