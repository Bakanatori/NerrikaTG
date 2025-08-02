package com.shindaq.NerrikaTG;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = NerrikaCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<String> BOT_TOKEN = BUILDER
            .comment("Telegram Bot Token")
            .define("general.botToken", "your bot token");

    private static final ForgeConfigSpec.LongValue CHAT_ID = BUILDER
            .comment("Telegram Chat ID")
            .defineInRange("general.chatId", 0L, Long.MIN_VALUE, Long.MAX_VALUE);

    private static final ForgeConfigSpec.LongValue TOPIC_ID = BUILDER
            .comment("Telegram Topic ID (optional, set to 0 to disable)")
            .defineInRange("general.topicId", 0L, 0L, Long.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static String botToken;
    public static long chatId;
    public static long topicId;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        botToken = BOT_TOKEN.get();
        chatId = CHAT_ID.get();
        topicId = TOPIC_ID.get();
    }
}
