package none.jerrycyx.undeadfireweakness;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(UndeadFireWeakness.MODID)
public class UndeadFireWeakness {
	public static final String MODID = "undead_fire_weakness";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public UndeadFireWeakness(FMLJavaModLoadingContext context) {
		MinecraftForge.EVENT_BUS.addListener(Config::onTagsUpdate);
		MinecraftForge.EVENT_BUS.addListener(UndeadFireWeakness::OnEntityHurt);
		MinecraftForge.EVENT_BUS.register(this);
		context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
	}
	
	public static void OnEntityHurt(LivingHurtEvent event) {
		if (Config.checkEntityType(event.getEntity()) && Config.checkDamageType(event.getSource().type())
				&& (!Config.mustByPlayer || event.getSource().getEntity() instanceof Player)) {
			event.setAmount((float) (event.getAmount() * Config.valueMultiplier + Config.valueAddition));
		}
	}
}
