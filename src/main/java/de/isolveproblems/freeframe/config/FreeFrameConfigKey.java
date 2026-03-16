package de.isolveproblems.freeframe.config;

import de.isolveproblems.freeframe.api.ConfigEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public enum FreeFrameConfigKey implements ConfigEntry {
    FREEFRAME_PREFIX("freeframe.prefix", "&eFreeFrame &8>>"),
    FREEFRAME_RELOAD_PERMISSION("freeframe.reload.permission", "freeframe.reload"),
    FREEFRAME_RELOAD_MESSAGE("freeframe.reload.message", "%prefix% &aFreeFrame successfully reloaded all configs."),
    FREEFRAME_ADMIN_PERMISSION("freeframe.admin.permission", "freeframe.admin"),
    FREEFRAME_ADMIN_LISTPAGESIZE("freeframe.admin.listPageSize", 8),
    FREEFRAME_ERROR_PERMISSION("freeframe.error.permission", "%prefix% &cYou don't have enough permissions to perform this command."),
    FREEFRAME_ERROR_UNKNOWNFRAME("freeframe.error.unknownFrame", "%prefix% &cNo FreeFrame found for id &e%id%&c."),
    FREEFRAME_DESTROY_PERMISSION("freeframe.destroy.permission", "freeframe.destroy"),
    FREEFRAME_DESTROY_MESSAGE("freeframe.destroy.message", "%prefix% &eYou've destroyed the &6FreeFrame &esuccessfully."),
    FREEFRAME_DESTROY_HAVETOSNEAK("freeframe.destroy.haveToSneak", "%prefix% &cYou have to sneak if you want to destroy this FreeFrame."),
    FREEFRAME_DESTROY_GAMEMODE("freeframe.destroy.gamemode", "%prefix% &cYou have to be in creative mode to destroy this FreeFrame."),
    FREEFRAME_ITEM_AMOUNT("freeframe.item.amount", 1),
    FREEFRAME_DEFAULT_PRICE("freeframe.default.price", 0.0D),
    FREEFRAME_DEFAULT_CURRENCY("freeframe.default.currency", "$"),
    FREEFRAME_GUI_INVENTORY_SIZE("freeframe.gui.inventory.size", 9),
    FREEFRAME_GUI_TITLE("freeframe.gui.title", "%prefix%"),
    FREEFRAME_GUI_SALESLOTS("freeframe.gui.saleSlots", Arrays.asList(2, 4, 6)),
    FREEFRAME_GUI_CLOSEAFTERPURCHASE("freeframe.gui.closeAfterPurchase", false),
    FREEFRAME_GUI_DROPONFULLINVENTORY("freeframe.gui.dropOnFullInventory", true),
    FREEFRAME_PROFILES_AMOUNTS("freeframe.profiles.amounts", Arrays.asList(1, 16, 64)),
    FREEFRAME_PROFILES_PRICEMULTIPLIERS("freeframe.profiles.priceMultipliers", Arrays.asList(1.0D, 16.0D, 64.0D)),
    FREEFRAME_TYPES_DEFAULT("freeframe.types.default", "SHOP"),
    FREEFRAME_TYPES_ADMINONLYPERMISSION("freeframe.types.adminOnlyPermission", "freeframe.adminonly"),
    FREEFRAME_SALEMODE_DEFAULT("freeframe.saleMode.default", "INSTANT"),
    FREEFRAME_SHOPS_OFFERMODE("freeframe.shops.offerMode", "BOTH"),
    FREEFRAME_OWNERMANAGEMENT_ENABLED("freeframe.ownerManagement.enabled", true),
    FREEFRAME_CHESTRESTOCK_ENABLED("freeframe.chestRestock.enabled", true),
    FREEFRAME_CHESTRESTOCK_REQUIRELINKEDCHEST("freeframe.chestRestock.requireLinkedChest", false),
    FREEFRAME_CHESTRESTOCK_ROUTE_SCANRADIUS("freeframe.chestRestock.route.scanRadius", 2),
    FREEFRAME_CHESTRESTOCK_ROUTE_NETWORKENABLED("freeframe.chestRestock.route.networkEnabled", true),
    FREEFRAME_ACCESS_REQUIREOWNER("freeframe.access.requireOwner", false),
    FREEFRAME_ACCESS_BYPASSPERMISSION("freeframe.access.bypassPermission", "freeframe.access.bypass"),
    FREEFRAME_ACCESS_DENIED("freeframe.access.denied", "%prefix% &cYou are not allowed to use this FreeFrame."),
    FREEFRAME_COMPAT_ARMORSTANDAMOUNTFIX("freeframe.compat.armorStandAmountFix", true),
    FREEFRAME_COMPAT_CANCELROTATION("freeframe.compat.cancelRotation", true),
    FREEFRAME_COOLDOWN_PLAYERMILLIS("freeframe.cooldown.playerMillis", 300L),
    FREEFRAME_COOLDOWN_FRAMEMILLIS("freeframe.cooldown.frameMillis", 100L),
    FREEFRAME_COOLDOWN_MESSAGE("freeframe.cooldown.message", "%prefix% &cPlease wait before using another FreeFrame."),
    FREEFRAME_RATELIMIT_MESSAGE("freeframe.rateLimit.message", "%prefix% &cThis FreeFrame is currently rate-limited."),
    FREEFRAME_PURCHASE_SUCCESS("freeframe.purchase.success", "%prefix% &aPurchased item for &e%currency%%price%&a."),
    FREEFRAME_PURCHASE_FREE("freeframe.purchase.free", "%prefix% &aYou received this item for free."),
    FREEFRAME_PURCHASE_NOTENOUGHMONEY("freeframe.purchase.notEnoughMoney", "%prefix% &cNot enough money. Required: &e%currency%%price%&c."),
    FREEFRAME_PURCHASE_ECONOMYUNAVAILABLE("freeframe.purchase.economyUnavailable", "%prefix% &cEconomy is not available right now."),
    FREEFRAME_PURCHASE_INVENTORYDROP("freeframe.purchase.inventoryDrop", "%prefix% &eYour inventory was full. Remaining items were dropped."),
    FREEFRAME_PURCHASE_INVENTORYFULL("freeframe.purchase.inventoryFull", "%prefix% &cYour inventory is full."),
    FREEFRAME_PURCHASE_STOCKOUT("freeframe.purchase.stockOut", "%prefix% &cThis frame is out of stock."),
    FREEFRAME_PURCHASE_LIMITED("freeframe.purchase.limited", "%prefix% &cPurchase limit reached. Try again later."),
    FREEFRAME_PURCHASE_BUSY("freeframe.purchase.busy", "%prefix% &cThis frame is processing another transaction."),
    FREEFRAME_SHOPS_OFFERFILTERED("freeframe.shops.offerFiltered", "%prefix% &cThis shop type is currently disabled."),
    FREEFRAME_SECURITY_INVALID("freeframe.security.invalid", "%prefix% &cTransaction signature invalid."),
    FREEFRAME_SECURITY_DUPLICATE("freeframe.security.duplicate", "%prefix% &eDuplicate purchase blocked."),
    FREEFRAME_REPUTATION_ENABLED("freeframe.reputation.enabled", true),
    FREEFRAME_REPUTATION_BLOCKTHRESHOLD("freeframe.reputation.blockThreshold", 85.0D),
    FREEFRAME_REPUTATION_HIGHVALUETHRESHOLD("freeframe.reputation.highValueThreshold", 500.0D),
    FREEFRAME_REPUTATION_BLOCKED("freeframe.reputation.blocked", "%prefix% &cPurchase blocked by fraud protection."),
    FREEFRAME_REPUTATION_WEIGHTS_FAILURE("freeframe.reputation.weights.failure", 8.0D),
    FREEFRAME_REPUTATION_WEIGHTS_DUPLICATE("freeframe.reputation.weights.duplicate", 5.0D),
    FREEFRAME_REPUTATION_WEIGHTS_INVALIDSIGNATURE("freeframe.reputation.weights.invalidSignature", 16.0D),
    FREEFRAME_REPUTATION_WEIGHTS_SUCCESSDECAY("freeframe.reputation.weights.successDecay", 0.25D),
    FREEFRAME_REPUTATION_WEIGHTS_HIGHVALUEPURCHASE("freeframe.reputation.weights.highValuePurchase", 4.0D),
    FREEFRAME_FRAME_INACTIVE("freeframe.frame.inactive", "%prefix% &cThis FreeFrame is currently inactive."),
    FREEFRAME_TYPES_PREVIEWONLY("freeframe.types.previewOnly", "%prefix% &7This frame is preview-only."),
    FREEFRAME_TYPES_ADMINONLYDENIED("freeframe.types.adminOnlyDenied", "%prefix% &cThis frame is restricted to admins."),
    FREEFRAME_AUCTION_INVALID("freeframe.auction.invalid", "%prefix% &cInvalid auction request."),
    FREEFRAME_AUCTION_NOTENABLED("freeframe.auction.notEnabled", "%prefix% &cThis frame is not in auction mode."),
    FREEFRAME_AUCTION_USEBID("freeframe.auction.useBid", "%prefix% &eThis frame is in auction mode. Use /freeframe bid <id> <amount>."),
    FREEFRAME_AUCTION_ENDED("freeframe.auction.ended", "%prefix% &cAuction already ended."),
    FREEFRAME_AUCTION_BIDTOOLOW("freeframe.auction.bidTooLow", "%prefix% &cBid too low. Minimum is &e%price%&c."),
    FREEFRAME_AUCTION_BIDACCEPTED("freeframe.auction.bidAccepted", "%prefix% &aBid accepted: &e%price%&a."),
    FREEFRAME_METRICS_BSTATSPLUGINID("freeframe.metrics.bstatsPluginId", 0),
    FREEFRAME_PLACEHOLDERAPI_ENABLED("freeframe.placeholderapi.enabled", true),
    FREEFRAME_ECONOMY_ALLOWWITHOUTVAULT("freeframe.economy.allowWithoutVault", false),
    FREEFRAME_ECONOMY_PAYOWNER("freeframe.economy.payOwner", true),
    FREEFRAME_ECONOMY_PAYOWNERONSELFPURCHASE("freeframe.economy.payOwnerOnSelfPurchase", false),
    FREEFRAME_DISCOUNTS_PERMISSIONS("freeframe.discounts.permissions", Collections.emptyMap()),
    FREEFRAME_DISCOUNTS_PERMISSIONS_FREEFRAME_DISCOUNT_VIP("freeframe.discounts.permissions.freeframe.discount.vip", 10.0D),
    FREEFRAME_DISCOUNTS_PERMISSIONS_FREEFRAME_DISCOUNT_MVP("freeframe.discounts.permissions.freeframe.discount.mvp", 25.0D),
    FREEFRAME_TAX_ENABLED("freeframe.tax.enabled", false),
    FREEFRAME_TAX_DEFAULTPERCENT("freeframe.tax.defaultPercent", 5.0D),
    FREEFRAME_TAX_ADMINSHOPPERCENT("freeframe.tax.adminShopPercent", 5.0D),
    FREEFRAME_TAX_USERSHOPPERCENT("freeframe.tax.userShopPercent", 5.0D),
    FREEFRAME_TAX_ROUNDTOCENTS("freeframe.tax.roundToCents", true),
    FREEFRAME_TAX_DEPOSITTOACCOUNT("freeframe.tax.depositToAccount", false),
    FREEFRAME_TAX_SERVERACCOUNTNAME("freeframe.tax.serverAccountName", "server"),
    FREEFRAME_DYNAMICPRICING_ENABLED("freeframe.dynamicPricing.enabled", false),
    FREEFRAME_DYNAMICPRICING_WINDOWMILLIS("freeframe.dynamicPricing.windowMillis", 600000L),
    FREEFRAME_DYNAMICPRICING_DEMANDTHRESHOLD("freeframe.dynamicPricing.demandThreshold", 5),
    FREEFRAME_DYNAMICPRICING_DEMANDSTEPPERCENT("freeframe.dynamicPricing.demandStepPercent", 2.5D),
    FREEFRAME_DYNAMICPRICING_LOWSTOCKTHRESHOLDPERCENT("freeframe.dynamicPricing.lowStockThresholdPercent", 20.0D),
    FREEFRAME_DYNAMICPRICING_LOWSTOCKBONUSPERCENT("freeframe.dynamicPricing.lowStockBonusPercent", 5.0D),
    FREEFRAME_DYNAMICPRICING_MINMULTIPLIER("freeframe.dynamicPricing.minMultiplier", 0.75D),
    FREEFRAME_DYNAMICPRICING_MAXMULTIPLIER("freeframe.dynamicPricing.maxMultiplier", 2.50D),
    FREEFRAME_SEASONS_ENABLED("freeframe.seasons.enabled", false),
    FREEFRAME_SEASONS_TIMEZONE("freeframe.seasons.timezone", "UTC"),
    FREEFRAME_SEASONS_RULES("freeframe.seasons.rules", Collections.emptyMap()),
    FREEFRAME_SEASONS_RULES_DEFAULT_ENABLED("freeframe.seasons.rules.default.enabled", false),
    FREEFRAME_SEASONS_RULES_DEFAULT_START("freeframe.seasons.rules.default.start", "2026-01-01T00:00"),
    FREEFRAME_SEASONS_RULES_DEFAULT_END("freeframe.seasons.rules.default.end", "2026-12-31T23:59"),
    FREEFRAME_SEASONS_RULES_DEFAULT_PRICEMULTIPLIER("freeframe.seasons.rules.default.priceMultiplier", 1.0D),
    FREEFRAME_SEASONS_RULES_DEFAULT_TAXOVERRIDEPERCENT("freeframe.seasons.rules.default.taxOverridePercent", 5.0D),
    FREEFRAME_AUCTION_ENABLED("freeframe.auction.enabled", true),
    FREEFRAME_AUCTION_TICKINTERVALTICKS("freeframe.auction.tickIntervalTicks", 40L),
    FREEFRAME_AUCTION_OFFLINEGRACEMILLIS("freeframe.auction.offlineGraceMillis", 300000L),
    FREEFRAME_AUCTION_OFFLINEMAXEXTENSIONS("freeframe.auction.offlineMaxExtensions", 3),
    FREEFRAME_ALERTS_ENABLED("freeframe.alerts.enabled", false),
    FREEFRAME_ALERTS_COOLDOWNMILLIS("freeframe.alerts.cooldownMillis", 120000L),
    FREEFRAME_ALERTS_LOWSTOCKTHRESHOLD("freeframe.alerts.lowStockThreshold", 5),
    FREEFRAME_STOCK_DEFAULT("freeframe.stock.default", 64),
    FREEFRAME_STOCK_DEFAULTMAX("freeframe.stock.defaultMax", 64),
    FREEFRAME_STOCK_AUTOREFILL_DEFAULTENABLED("freeframe.stock.autoRefill.defaultEnabled", false),
    FREEFRAME_STOCK_AUTOREFILL_DEFAULTINTERVALMILLIS("freeframe.stock.autoRefill.defaultIntervalMillis", 300000L),
    FREEFRAME_LIMITS_ENABLED("freeframe.limits.enabled", false),
    FREEFRAME_LIMITS_MAXITEMSPERWINDOW("freeframe.limits.maxItemsPerWindow", 64),
    FREEFRAME_LIMITS_WINDOWMILLIS("freeframe.limits.windowMillis", 600000L),
    FREEFRAME_ITEMS_MODE("freeframe.items.mode", "off"),
    FREEFRAME_ITEMS_BLACKLIST("freeframe.items.blacklist", Collections.emptyList()),
    FREEFRAME_ITEMS_WHITELIST("freeframe.items.whitelist", Collections.emptyList()),
    FREEFRAME_ITEMS_BLOCKEDMESSAGE("freeframe.items.blockedMessage", "%prefix% &cThis item type is blocked by item policy."),
    FREEFRAME_LOGGING_ENABLED("freeframe.logging.enabled", true),
    FREEFRAME_LOGGING_PURCHASEENABLED("freeframe.logging.purchaseEnabled", true),
    FREEFRAME_LOGGING_ADMINENABLED("freeframe.logging.adminEnabled", true),
    FREEFRAME_LOGGING_DIRECTORY("freeframe.logging.directory", "logs"),
    FREEFRAME_LOGGING_FILEPREFIX("freeframe.logging.filePrefix", "audit"),
    FREEFRAME_LOGGING_EXTENSION("freeframe.logging.extension", ".csv"),
    FREEFRAME_LOGGING_EXPORTDIRECTORY("freeframe.logging.exportDirectory", "exports"),
    FREEFRAME_WEBHOOKS_ENABLED("freeframe.webhooks.enabled", false),
    FREEFRAME_WEBHOOKS_DISCORDURL("freeframe.webhooks.discordUrl", ""),
    FREEFRAME_WEBHOOKS_ENDPOINTS("freeframe.webhooks.endpoints", Collections.emptyList()),
    FREEFRAME_WEBHOOKS_SCHEMAVERSION("freeframe.webhooks.schemaVersion", "2.0"),
    FREEFRAME_WEBHOOKS_SECRET("freeframe.webhooks.secret", ""),
    FREEFRAME_WEBHOOKS_TIMEOUTMILLIS("freeframe.webhooks.timeoutMillis", 4000),
    FREEFRAME_WEBHOOKS_MAXRETRIES("freeframe.webhooks.maxRetries", 3),
    FREEFRAME_WEBHOOKS_RETRYDELAYMILLIS("freeframe.webhooks.retryDelayMillis", 2000L),
    FREEFRAME_DISPLAY_ENABLED("freeframe.display.enabled", true),
    FREEFRAME_DISPLAY_REMOVEONDISABLE("freeframe.display.removeOnDisable", false),
    FREEFRAME_DISPLAY_TEMPLATE("freeframe.display.template", "&e%item% &7| &6%currency%%price% &7| &bStock: %stock%"),
    FREEFRAME_DISPLAY_ONLYWHENINSTOCK("freeframe.display.onlyWhenInStock", false),
    FREEFRAME_DISPLAY_LOADCHUNK("freeframe.display.loadChunk", false),
    FREEFRAME_DISPLAY_OFFSET_X("freeframe.display.offset.x", 0.5D),
    FREEFRAME_DISPLAY_OFFSET_Y("freeframe.display.offset.y", 0.45D),
    FREEFRAME_DISPLAY_OFFSET_Z("freeframe.display.offset.z", 0.5D),
    FREEFRAME_DISPLAY_ARMORSTAND_VISIBLE("freeframe.display.armorStand.visible", false),
    FREEFRAME_DISPLAY_ARMORSTAND_GRAVITY("freeframe.display.armorStand.gravity", false),
    FREEFRAME_DISPLAY_ARMORSTAND_SMALL("freeframe.display.armorStand.small", true),
    FREEFRAME_DISPLAY_ARMORSTAND_MARKER("freeframe.display.armorStand.marker", true),
    FREEFRAME_DISPLAY_ARMORSTAND_CUSTOMNAMEVISIBLE("freeframe.display.armorStand.customNameVisible", true),
    FREEFRAME_BRANDING_ENABLED("freeframe.branding.enabled", true),
    FREEFRAME_BRANDING_DEFAULTTHEME("freeframe.branding.defaultTheme", "classic"),
    FREEFRAME_BRANDING_DEFAULTADMINTHEME("freeframe.branding.defaultAdminTheme", "admin"),
    FREEFRAME_BRANDING_DEFAULTUSERTHEME("freeframe.branding.defaultUserTheme", "classic"),
    FREEFRAME_BRANDING_THEMES("freeframe.branding.themes", Collections.emptyMap()),
    FREEFRAME_BRANDING_THEMES_CLASSIC_DISPLAYTEMPLATE("freeframe.branding.themes.classic.displayTemplate", "&e%item% &7| &6%currency%%price% &7| &bStock: %stock%"),
    FREEFRAME_BRANDING_THEMES_ADMIN_DISPLAYTEMPLATE("freeframe.branding.themes.admin.displayTemplate", "&c[ADMIN] &f%item% &8| &6%currency%%price% &8| &bStock:%stock%"),
    FREEFRAME_BRANDING_THEMES_SEASONAL_DISPLAYTEMPLATE("freeframe.branding.themes.seasonal.displayTemplate", "&d[%campaign%] &f%item% &8| &6%currency%%price% &8| &bStock:%stock%"),
    FREEFRAME_RESTRICTIONS_DENIED("freeframe.restrictions.denied", "%prefix% &cFreeFrame is disabled in this world/region."),
    FREEFRAME_RESTRICTIONS_WORLDS_ENABLED("freeframe.restrictions.worlds.enabled", false),
    FREEFRAME_RESTRICTIONS_WORLDS_MODE("freeframe.restrictions.worlds.mode", "whitelist"),
    FREEFRAME_RESTRICTIONS_WORLDS_LIST("freeframe.restrictions.worlds.list", Collections.emptyList()),
    FREEFRAME_RESTRICTIONS_REGIONS_ENABLED("freeframe.restrictions.regions.enabled", false),
    FREEFRAME_RESTRICTIONS_REGIONS_LIST("freeframe.restrictions.regions.list", Collections.emptyList()),
    FREEFRAME_INTEGRATIONS_WORLDGUARD_ENABLED("freeframe.integrations.worldguard.enabled", true),
    FREEFRAME_INTEGRATIONS_WORLDGUARD_REQUIREDFLAG("freeframe.integrations.worldguard.requiredFlag", ""),
    FREEFRAME_INTEGRATIONS_GRIEFPREVENTION_ENABLED("freeframe.integrations.griefprevention.enabled", true),
    FREEFRAME_INTEGRATIONS_GRIEFPREVENTION_MODE("freeframe.integrations.griefprevention.mode", "allow-any"),
    FREEFRAME_LOCALIZATION_DEFAULTLOCALE("freeframe.localization.defaultLocale", "en"),
    FREEFRAME_LOCALIZATION_USEPLAYERLOCALE("freeframe.localization.usePlayerLocale", true),
    FREEFRAME_SETUP_MESSAGES_EMPTYFRAME("freeframe.setup.messages.emptyFrame", "%prefix% &cThe selected ItemFrame is empty."),
    FREEFRAME_SETUP_WANDNAME("freeframe.setup.wandName", "&6FreeFrame Setup Wand"),
    FREEFRAME_SETUP_WANDRECEIVED("freeframe.setup.wandReceived", "%prefix% &aSetup wand received."),
    FREEFRAME_SETUP_WANDMATERIAL("freeframe.setup.wandMaterial", "BLAZE_ROD"),
    FREEFRAME_SETUP_WANDAMOUNT("freeframe.setup.wandAmount", 1),
    FREEFRAME_SETUP_EDITOR_INVENTORYSIZE("freeframe.setup.editor.inventorySize", 27),
    FREEFRAME_SETUP_EDITOR_TITLE("freeframe.setup.editor.title", "&8FreeFrame Setup: &e%id%"),
    FREEFRAME_SETUP_EDITOR_STOCKSTEP("freeframe.setup.editor.stockStep", 1),
    FREEFRAME_SETUP_EDITOR_MAXSTOCKSTEP("freeframe.setup.editor.maxStockStep", 8),
    FREEFRAME_SETUP_EDITOR_MAXSTOCKCAP("freeframe.setup.editor.maxStockCap", 4096),
    FREEFRAME_SETUP_EDITOR_PRICESTEP("freeframe.setup.editor.priceStep", 1.0D),
    FREEFRAME_SETUP_EDITOR_CLOSEAFTERCHANGE("freeframe.setup.editor.closeAfterChange", false),
    FREEFRAME_SETUP_EDITOR_REFRESHDISPLAY("freeframe.setup.editor.refreshDisplay", true),
    FREEFRAME_SETUP_EDITOR_SLOTS_INFO("freeframe.setup.editor.slots.info", 4),
    FREEFRAME_SETUP_EDITOR_SLOTS_TOGGLEACTIVE("freeframe.setup.editor.slots.toggleActive", 10),
    FREEFRAME_SETUP_EDITOR_SLOTS_STOCKDOWN("freeframe.setup.editor.slots.stockDown", 11),
    FREEFRAME_SETUP_EDITOR_SLOTS_STOCKUP("freeframe.setup.editor.slots.stockUp", 12),
    FREEFRAME_SETUP_EDITOR_SLOTS_REFILL("freeframe.setup.editor.slots.refill", 13),
    FREEFRAME_SETUP_EDITOR_SLOTS_PRICEDOWN("freeframe.setup.editor.slots.priceDown", 14),
    FREEFRAME_SETUP_EDITOR_SLOTS_PRICEUP("freeframe.setup.editor.slots.priceUp", 15),
    FREEFRAME_SETUP_EDITOR_SLOTS_TOGGLEAUTOREFILL("freeframe.setup.editor.slots.toggleAutoRefill", 16),
    FREEFRAME_SETUP_EDITOR_SLOTS_MAXSTOCKDOWN("freeframe.setup.editor.slots.maxStockDown", 19),
    FREEFRAME_SETUP_EDITOR_SLOTS_MAXSTOCKUP("freeframe.setup.editor.slots.maxStockUp", 20),
    FREEFRAME_SETUP_EDITOR_SLOTS_CLOSE("freeframe.setup.editor.slots.close", 22),
    FREEFRAME_SETUP_EDITOR_MATERIALS_INFO("freeframe.setup.editor.materials.info", "PAPER"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_TOGGLEACTIVEON("freeframe.setup.editor.materials.toggleActiveOn", "EMERALD_BLOCK"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_TOGGLEACTIVEOFF("freeframe.setup.editor.materials.toggleActiveOff", "REDSTONE_BLOCK"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_STOCKDOWN("freeframe.setup.editor.materials.stockDown", "REDSTONE"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_STOCKUP("freeframe.setup.editor.materials.stockUp", "EMERALD"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_REFILL("freeframe.setup.editor.materials.refill", "CHEST"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_PRICEDOWN("freeframe.setup.editor.materials.priceDown", "GOLD_NUGGET"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_PRICEUP("freeframe.setup.editor.materials.priceUp", "GOLD_INGOT"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_TOGGLEAUTOREFILL("freeframe.setup.editor.materials.toggleAutoRefill", "LEVER"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_MAXSTOCKDOWN("freeframe.setup.editor.materials.maxStockDown", "COAL"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_MAXSTOCKUP("freeframe.setup.editor.materials.maxStockUp", "DIAMOND"),
    FREEFRAME_SETUP_EDITOR_MATERIALS_CLOSE("freeframe.setup.editor.materials.close", "BARRIER"),
    FREEFRAME_STORAGE_TYPE("freeframe.storage.type", "yaml"),
    FREEFRAME_STORAGE_MIGRATEONSWITCH("freeframe.storage.migrateOnSwitch", true),
    FREEFRAME_STORAGE_SQLITE_FILE("freeframe.storage.sqlite.file", "freeframe.db"),
    FREEFRAME_STORAGE_ASYNCQUEUE_ENABLED("freeframe.storage.asyncQueue.enabled", true),
    FREEFRAME_STORAGE_MYSQL_HOST("freeframe.storage.mysql.host", "127.0.0.1"),
    FREEFRAME_STORAGE_MYSQL_PORT("freeframe.storage.mysql.port", 3306),
    FREEFRAME_STORAGE_MYSQL_DATABASE("freeframe.storage.mysql.database", "freeframe"),
    FREEFRAME_STORAGE_MYSQL_USERNAME("freeframe.storage.mysql.username", "root"),
    FREEFRAME_STORAGE_MYSQL_PASSWORD("freeframe.storage.mysql.password", ""),
    FREEFRAME_STORAGE_MYSQL_SSL("freeframe.storage.mysql.ssl", false),
    FREEFRAME_STORAGE_MYSQL_TABLE("freeframe.storage.mysql.table", "freeframe_frames"),
    FREEFRAME_FRAMES("freeframe.frames", Collections.emptyList()),
    FREEFRAME_FRAMESDATA("freeframe.framesData", Collections.emptyMap()),
    FREEFRAME_BACKUP_CREATED("freeframe.backup.created", "%prefix% &aBackup created: &e%file%&a."),
    FREEFRAME_BACKUP_RESTORED("freeframe.backup.restored", "%prefix% &aBackup restored: &e%file%&a."),
    FREEFRAME_BACKUP_FAILED("freeframe.backup.failed", "%prefix% &cBackup action failed."),
    FREEFRAME_STATS_HEADER("freeframe.stats.header", "%prefix% &6Statistics for &e%target%&6:"),
    FREEFRAME_DASHBOARD_ENABLED("freeframe.dashboard.enabled", false),
    FREEFRAME_DASHBOARD_HOST("freeframe.dashboard.host", "127.0.0.1"),
    FREEFRAME_DASHBOARD_PORT("freeframe.dashboard.port", 8095),
    FREEFRAME_DASHBOARD_TOKEN("freeframe.dashboard.token", ""),
    FREEFRAME_SECURITY_SECRET("freeframe.security.secret", ""),
    FREEFRAME_SECURITY_IDEMPOTENCYBUCKETMILLIS("freeframe.security.idempotencyBucketMillis", 1500L),
    FREEFRAME_CAMPAIGNS_ENABLED("freeframe.campaigns.enabled", true),
    FREEFRAME_CAMPAIGNS_TIMEZONE("freeframe.campaigns.timezone", "UTC"),
    FREEFRAME_CAMPAIGNS_DEFAULTRULE("freeframe.campaigns.defaultRule", ""),
    FREEFRAME_CAMPAIGNS_RULES("freeframe.campaigns.rules", Collections.emptyMap()),
    FREEFRAME_CAMPAIGNS_RULES_FLASH_ENABLED("freeframe.campaigns.rules.flash.enabled", false),
    FREEFRAME_CAMPAIGNS_RULES_FLASH_START("freeframe.campaigns.rules.flash.start", "2026-01-01T00:00"),
    FREEFRAME_CAMPAIGNS_RULES_FLASH_END("freeframe.campaigns.rules.flash.end", "2026-12-31T23:59"),
    FREEFRAME_CAMPAIGNS_RULES_FLASH_PRICEMULTIPLIER("freeframe.campaigns.rules.flash.priceMultiplier", 0.90D),
    FREEFRAME_CAMPAIGNS_RULES_FLASH_TAXOVERRIDEPERCENT("freeframe.campaigns.rules.flash.taxOverridePercent", 2.5D),
    FREEFRAME_CAMPAIGNS_RULES_FLASH_BRANDINGOVERRIDE("freeframe.campaigns.rules.flash.brandingOverride", "seasonal"),
    FREEFRAME_MODERATION_ENABLED("freeframe.moderation.enabled", true),
    FREEFRAME_MODERATION_ALLOWOWNERFRAMEFREEZE("freeframe.moderation.allowOwnerFrameFreeze", true),
    FREEFRAME_NETWORKSYNC_ENABLED("freeframe.networkSync.enabled", false),
    FREEFRAME_NETWORKSYNC_MODE("freeframe.networkSync.mode", "none"),
    FREEFRAME_NETWORKSYNC_BRIDGECHANNEL("freeframe.networkSync.bridgeChannel", "freeframe-sync"),
    FREEFRAME_NETWORKSYNC_FILEPOLLTICKS("freeframe.networkSync.filePollTicks", 100L),
    FREEFRAME_NETWORKSYNC_EVENTTTLMILLIS("freeframe.networkSync.eventTtlMillis", 180000L),
    FREEFRAME_NETWORKSYNC_APPLYSTOCK("freeframe.networkSync.applyStock", true),
    FREEFRAME_NETWORKSYNC_APPLYPRICE("freeframe.networkSync.applyPrice", false),
    FREEFRAME_NETWORKSYNC_APPLYREVENUE("freeframe.networkSync.applyRevenue", false),
    FREEFRAME_PROXY_BUNGEECORD_ENABLED("freeframe.proxy.bungeecord.enabled", true),
    FREEFRAME_PROXY_VELOCITY_ENABLED("freeframe.proxy.velocity.enabled", true),
    FREEFRAME_PROXY_VELOCITY_CHANNEL("freeframe.proxy.velocity.channel", "freeframe:sync"),
    FREEFRAME_ANALYTICS_ENABLED("freeframe.analytics.enabled", true),
    FREEFRAME_MIGRATION_ZERODOWNTIME_ENABLED("freeframe.migration.zeroDowntime.enabled", true),
    FREEFRAME_SETUP_WANDLORE("freeframe.setup.wandLore", Arrays.asList(
        "&7Right-click an ItemFrame to open the editor.",
        "&7Requires admin permission."
    ));

    private final String path;
    private final Object defaultValue;
    private final List<String> comments;

    FreeFrameConfigKey(String path, Object defaultValue, String... comments) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.comments = buildComments(path, defaultValue, comments);
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public Object defaultValue() {
        return this.defaultValue;
    }

    @Override
    public List<String> comments() {
        return this.comments;
    }

    private static List<String> buildComments(String path, Object defaultValue, String[] provided) {
        List<String> comments = new ArrayList<String>();
        if (provided != null) {
            for (String line : provided) {
                if (line != null && !line.trim().isEmpty()) {
                    comments.add(line.trim());
                }
            }
        }
        if (!comments.isEmpty()) {
            return Collections.unmodifiableList(comments);
        }
        comments.add(autoComment(path));
        comments.add("Default: " + String.valueOf(defaultValue));
        return Collections.unmodifiableList(comments);
    }

    private static String autoComment(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "Config key";
        }
        String withoutRoot = path.startsWith("freeframe.") ? path.substring("freeframe.".length()) : path;
        String[] parts = withoutRoot.split("\\.");
        String last = parts[parts.length - 1];
        String normalized = last.replaceAll("([a-z])([A-Z])", "$1 $2").replace("_", " ").replace("-", " ").toLowerCase(Locale.ENGLISH);
        if (normalized.isEmpty()) {
            normalized = withoutRoot.toLowerCase(Locale.ENGLISH);
        }
        return "Controls " + normalized + ".";
    }
}
