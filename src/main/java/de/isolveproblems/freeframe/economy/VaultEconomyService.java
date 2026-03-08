package de.isolveproblems.freeframe.economy;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
            if (response == null) {
                return EconomyChargeResult.error("withdrawPlayer returned null response");
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
        } catch (Exception exception) {
            return EconomyChargeResult.error(exception.getMessage());
        }
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
