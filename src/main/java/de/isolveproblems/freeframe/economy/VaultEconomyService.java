package de.isolveproblems.freeframe.economy;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class VaultEconomyService {
    private final FreeFrame freeframe;
    private Object economyProvider;

    public VaultEconomyService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public void initialize() {
        this.economyProvider = null;

        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            @SuppressWarnings("unchecked")
            Class<Object> rawClass = (Class<Object>) economyClass;
            RegisteredServiceProvider<Object> registration = this.freeframe.getServer()
                .getServicesManager()
                .getRegistration(rawClass);

            if (registration != null) {
                this.economyProvider = registration.getProvider();
            }
        } catch (ClassNotFoundException ignored) {
            this.economyProvider = null;
        } catch (Exception exception) {
            this.freeframe.getLogger().warning("Vault economy initialization failed: " + exception.getMessage());
            this.economyProvider = null;
        }
    }

    public boolean isAvailable() {
        return this.economyProvider != null;
    }

    public EconomyChargeResult charge(Player player, double amount) {
        if (amount <= 0.0D) {
            return EconomyChargeResult.success();
        }

        if (!this.isAvailable()) {
            return EconomyChargeResult.unavailable();
        }

        try {
            Object response = this.withdraw(player, amount);
            return this.toResult(response);
        } catch (Exception exception) {
            return EconomyChargeResult.error(exception.getMessage());
        }
    }

    public EconomyChargeResult depositToOwner(String ownerUuid, String ownerName, double amount) {
        if (amount <= 0.0D) {
            return EconomyChargeResult.success();
        }

        if (!this.isAvailable()) {
            return EconomyChargeResult.unavailable();
        }

        try {
            Object response = this.deposit(ownerUuid, ownerName, amount);
            return this.toResult(response);
        } catch (Exception exception) {
            return EconomyChargeResult.error(exception.getMessage());
        }
    }

    private EconomyChargeResult toResult(Object response) {
        if (response == null) {
            return EconomyChargeResult.error("economy response is null");
        }

        Object typeValue = this.readField(response, "type");
        String type = typeValue == null ? "" : String.valueOf(typeValue);
        if ("SUCCESS".equalsIgnoreCase(type)) {
            return EconomyChargeResult.success();
        }

        String message = String.valueOf(this.readField(response, "errorMessage"));
        if (type.toUpperCase().contains("FUNDS")) {
            return EconomyChargeResult.notEnoughMoney(message);
        }
        return EconomyChargeResult.error(message);
    }

    private Object withdraw(Player player, double amount) throws Exception {
        try {
            Method method = this.economyProvider.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            return method.invoke(this.economyProvider, player, amount);
        } catch (NoSuchMethodException ignored) {
            Method method = this.economyProvider.getClass().getMethod("withdrawPlayer", String.class, double.class);
            return method.invoke(this.economyProvider, player.getName(), amount);
        }
    }

    private Object deposit(String ownerUuid, String ownerName, double amount) throws Exception {
        OfflinePlayer offlinePlayer = this.resolveOfflinePlayer(ownerUuid, ownerName);

        if (offlinePlayer != null) {
            try {
                Method method = this.economyProvider.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class);
                return method.invoke(this.economyProvider, offlinePlayer, amount);
            } catch (NoSuchMethodException ignored) {
                // fallback to String API below
            }
        }

        Method legacy = this.economyProvider.getClass().getMethod("depositPlayer", String.class, double.class);
        return legacy.invoke(this.economyProvider, ownerName, amount);
    }

    private OfflinePlayer resolveOfflinePlayer(String ownerUuid, String ownerName) {
        try {
            if (ownerUuid != null && !ownerUuid.trim().isEmpty() && !"unknown".equalsIgnoreCase(ownerUuid)) {
                return Bukkit.getOfflinePlayer(UUID.fromString(ownerUuid));
            }
        } catch (Exception ignored) {
            // invalid UUID
        }

        try {
            if (ownerName != null && !ownerName.trim().isEmpty() && !"unknown".equalsIgnoreCase(ownerName)) {
                return Bukkit.getOfflinePlayer(ownerName);
            }
        } catch (Exception ignored) {
            // unavailable method/lookup
        }
        return null;
    }

    private Object readField(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception ignored) {
            return null;
        }
    }
}
