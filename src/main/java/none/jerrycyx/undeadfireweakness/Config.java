package none.jerrycyx.undeadfireweakness;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.*;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = UndeadFireWeakness.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
	private Config() {
		throw new RuntimeException(Config.class.getName() + " is not designed for instantiation.");
	}
	
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	private static final ForgeConfigSpec.BooleanValue MOD_ENABLED = BUILDER
			.comment("Whether to enable the mod")
			.define("mod_enabled", true);
	
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_TYPE_LIST = BUILDER
			.comment("Entity types that apply damage modifier must be included in this list.")
			.comment("Support entity type tags starting with \"#\" or write entity type directly")
			.comment("Use \"$all\" to match all entity types")
			.comment("Use \"$undead\" to match all undead")
			.defineListAllowEmpty("entity_type_list", List.of("$undead"), Config::validateTagOrResource);
	
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAMAGE_TYPE_LIST = BUILDER
			.comment("Damage types that be modified must be included in this list")
			.comment("Support damage type tags starting with \"#\" or write damage directly")
			.comment("Use \"$all\" to match all damage types")
			.defineListAllowEmpty("damage_type_list", List.of("#minecraft:is_fire"), Config::validateTagOrResource);
	
	private static final ForgeConfigSpec.BooleanValue MUST_BY_PLAYER = BUILDER
			.comment("Should the damage to modify must be caused by player.")
			.define("must_by_player", false);
	
	private static final ForgeConfigSpec.DoubleValue VALUE_MULTIPLIER = BUILDER
			.comment("The value to be multiply to the damage.")
			.comment("Works BEFORE addition.")
			.defineInRange("value_multiplier", 10.0, 0.0, Integer.MAX_VALUE);
	
	private static final ForgeConfigSpec.DoubleValue VALUE_ADDITION = BUILDER
			.comment("The value to be added to the damage.")
			.comment("Works AFTER multiplication.")
			.defineInRange("value_addition", 0.0, 0.0, Integer.MAX_VALUE);
	
	static final ForgeConfigSpec SPEC = BUILDER.build();
	
	private static boolean validateTagOrResource(final Object obj) {
		if (obj instanceof String tagOrResource) {
			if (tagOrResource.isEmpty()) return false;
			if (tagOrResource.equals("$all")) return true;
			if (tagOrResource.startsWith("#")) {
				tagOrResource = tagOrResource.substring(1);
			}
			ResourceLocation resourceLocation = ResourceLocation.tryParse(tagOrResource);
			return resourceLocation != null;
		} else return false;
	}
	
	public static boolean modEnabled;
	private static List<? extends String> entityTypeList;
	private static List<? extends String> damageTypeList;
	public static boolean mustByPlayer;
	public static double valueMultiplier;
	public static double valueAddition;
	
	private static Set<EntityType<?>> entityTypeCache;
	private static Set<DamageType> damageTypeCache;
	private static boolean allEntityTypes, undeadEntityTypes;
	private static boolean allDamageTypes;
	
	public static boolean checkEntityType(LivingEntity entity) {
		if (allEntityTypes) return true;
		else if (undeadEntityTypes && entity.getMobType().equals(MobType.UNDEAD)) return true;
		else return entityTypeCache.contains(entity.getType());
	}
	
	public static boolean checkDamageType(DamageType damageType) {
		if (allDamageTypes) return true;
		else return damageTypeCache.contains(damageType);
	}
	
	public static void onTagsUpdate(TagsUpdatedEvent event) {
		entityTypeCache = loadCache(event.getRegistryAccess().registryOrThrow(Registries.ENTITY_TYPE), entityTypeList, s -> {
			if (s.equals("$all")) allEntityTypes = true;
			if (s.equals("$undead")) undeadEntityTypes = true;
		});
		damageTypeCache = loadCache(event.getRegistryAccess().registryOrThrow(Registries.DAMAGE_TYPE), damageTypeList, s -> {
			if (s.equals("$all")) allDamageTypes = true;
		});
	}
	
	private static <V> Set<V> loadCache(Registry<V> registry, List<? extends String> list, Consumer<String> special) {
		Set<V> result = new HashSet<>();
		Set<ResourceLocation> tagSet = new HashSet<>();
		for (String tagOrResource : list) {
			if (tagOrResource.startsWith("$")) {
				special.accept(tagOrResource);
				continue;
			}
			boolean isTag = false;
			if (tagOrResource.startsWith("#")) {
				tagOrResource = tagOrResource.substring(1);
				isTag = true;
			}
			ResourceLocation resourceLocation = ResourceLocation.tryParse(tagOrResource);
			if (resourceLocation == null) {
				UndeadFireWeakness.LOGGER.warn("\"{}\" is not a valid format!", tagOrResource);
			}
			if (isTag) tagSet.add(resourceLocation);
			else {
				String finalTagOrResource = tagOrResource;
				registry.getOptional(resourceLocation).ifPresentOrElse(result::add,
						() -> UndeadFireWeakness.LOGGER.warn("\"{}\" is not exist!", finalTagOrResource));
			}
		}
		registry.getTags().filter(tagKeyNamedPair -> tagSet.contains(tagKeyNamedPair.getFirst().location()))
				.forEach(tagKeyNamedPair -> tagKeyNamedPair.getSecond().forEach(holder -> result.add(holder.value())));
		return result;
	}
	
	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		modEnabled = MOD_ENABLED.get();
		mustByPlayer = MUST_BY_PLAYER.get();
		entityTypeList = ENTITY_TYPE_LIST.get();
		damageTypeList = DAMAGE_TYPE_LIST.get();
		valueMultiplier = VALUE_MULTIPLIER.get();
		valueAddition = VALUE_ADDITION.get();
	}
}
