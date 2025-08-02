package com.shindaq.NerrikaTG;

import com.mojang.logging.LogUtils;

import com.shindaq.NerrikaTG.utils.ChatUtils;
import com.shindaq.NerrikaTG.utils.PlayerUtils;
import com.shindaq.NerrikaTG.utils.MessageUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import net.minecraft.network.chat.Component;
import java.util.concurrent.CompletableFuture;


@Mod(NerrikaCore.MODID)
public final class NerrikaCore {
    public static final String MODID = "nerrikatg";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NerrikaCore(FMLJavaModLoadingContext context) {
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // Get the mod event bus and register our common setup method
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("\n" +
                "\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                "...............................%%%%%%%%%%-.............................\n" +
                "...........................%%%%%%%%%%%%%%%%%%..........................\n" +
                "........................%%%%%%%%%%%%%%%%%%%%%%%%.......................\n" +
                "......................*%%%%%%%%%%%%%%%%%%%%%%%%%%#.....................\n" +
                ".....................%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%....................\n" +
                "...................*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%...................\n" +
                "..................%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%=..................\n" +
                "................#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%..................\n" +
                "...............%%%%%%%%%%%%%%%%%%%%%%%%%%%%%=%%%%%%%%%.................\n" +
                ".............=%%%%%%%%%%%%%%%%%%%%%@@*%%%%%@@@@:#%:#@%%................\n" +
                "............%%%%%%%%%%%%%%%*==:.@@@@@-%%%%%@@@@@@@@@@%%%...............\n" +
                "..........%%%%%%%%%%%%%%%%@@@@@@@@@@-%%%%%%@@@@@@@%%%%%%%-.............\n" +
                "..............%%%%%%%%%%%%%@@@@@@@@@%%%%%%%%%%%%%%%%%%%%%%%............\n" +
                ".............%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%..........\n" +
                "............%%%%=.%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%..%%%%%........\n" +
                ".................=%%%%%%%%%%%%%%%%%%%%%%:%%%%%%%%%%%:%%%%..............\n" +
                ".................%%%...%%%%%%%%%%%%%%%%%%%%%%%%%%%%%...=%%.............\n" +
                ".................=.....%%%%%%%%%%%%%%%%%%%%%%%%%%%%=...................\n" +
                "........................%%%%%%%%%%%%%%%%%%%%%...%%%....................\n" +
                "........................%%%....*%%%%%%%%%.......%%.....................\n" +
                "........................%%%.....................%......................\n" +
                ".........................%%............................................\n" +
                "..........................%............................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                ".......................................................................\n" +
                "\n");
        
        // Initialize Telegram service
        new TelegramService();
        
        if (Config.botToken.equals("your bot token")) {
            LOGGER.warn("Please configure your Telegram bot token in the config file!");
        } else if (Config.chatId == 0) {
            LOGGER.warn("Please configure your Telegram chat ID in the config file!");
        } else {
            LOGGER.info("Telegram integration configured successfully");
        }
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            MinecraftServer server = event.getServer();
            TelegramService.setMinecraftServer(server);
            
            // Запускаем Telegram сервис
            TelegramService telegramService = TelegramService.getInstance();
            if (telegramService != null) {
                telegramService.startPolling();
                
                // Отправляем уведомление о запуске сервера сразу
                telegramService.sendServerStartedNotification();
            }
            
            LOGGER.info("NerrikaTG: Server started, Telegram polling enabled");
        }
        
        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            // Отправляем уведомление об остановке сервера
            TelegramService telegramService = TelegramService.getInstance();
            if (telegramService != null) {
                try {
                    telegramService.sendServerStoppedNotification();
                } catch (Exception e) {
                    LOGGER.error("Ошибка при отправке уведомления об остановке сервера", e);
                }
                
                // Останавливаем polling
                telegramService.stopPolling();
            }
        }

        @SubscribeEvent
        public static void onServerChat(ServerChatEvent event) {
            ServerPlayer player = event.getPlayer();
            String message = event.getMessage().getString();
            String playerName = PlayerUtils.getSafePlayerName(player);

            TelegramService telegramService = TelegramService.getInstance();
            if (telegramService != null) {
                // Отправляем сообщение асинхронно для быстрого отклика
                CompletableFuture.runAsync(() -> {
                    try {
                        // Используем новый метод с жирным форматированием ника
                        telegramService.sendMinecraftMessage(playerName, message);
                    } catch (Exception e) {
                        LOGGER.error("Ошибка при отправке в Telegram", e);
                    }
                });
            }
        }
        
        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                String playerName = PlayerUtils.getSafePlayerName(player);
                
                // Отправляем сообщение только всем игрокам на сервере (НЕ в Telegram)
                MinecraftServer server = player.getServer();
                if (server != null) {
                    server.execute(() -> {
                        Component joinComponent = MessageUtils.createJoinMessage(playerName);
                        for (ServerPlayer onlinePlayer : PlayerUtils.getAllPlayers(server)) {
                            onlinePlayer.sendSystemMessage(joinComponent);
                        }
                    });
                }
            }
        }
        
        @SubscribeEvent
        public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                String playerName = PlayerUtils.getSafePlayerName(player);
                
                // Отправляем сообщение только всем игрокам на сервере (НЕ в Telegram)
                MinecraftServer server = player.getServer();
                if (server != null) {
                    server.execute(() -> {
                        Component leaveComponent = MessageUtils.createLeaveMessage(playerName);
                        for (ServerPlayer onlinePlayer : PlayerUtils.getAllPlayers(server)) {
                            onlinePlayer.sendSystemMessage(leaveComponent);
                        }
                    });
                }
            }
        }
    }
}
