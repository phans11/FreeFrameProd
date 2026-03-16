package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ShopOwnerType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxService {
    private final FreeFrame freeframe;

    public TaxService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public TaxBreakdown calculate(FreeFrameData frameData, double grossPrice, Double seasonalOverridePercent) {
        double gross = Math.max(0.0D, grossPrice);
        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_TAX_ENABLED) || gross <= 0.0D) {
            return new TaxBreakdown(gross, 0.0D, 0.0D);
        }

        double percent;
        if (seasonalOverridePercent != null && seasonalOverridePercent.doubleValue() >= 0.0D) {
            percent = seasonalOverridePercent.doubleValue();
        } else {
            ShopOwnerType ownerType = frameData == null ? ShopOwnerType.USER : frameData.getShopOwnerType();
            if (ownerType == ShopOwnerType.ADMIN) {
                percent = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_TAX_ADMINSHOPPERCENT);
            } else {
                percent = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_TAX_USERSHOPPERCENT);
            }
        }

        percent = Math.max(0.0D, Math.min(100.0D, percent));
        double tax = gross * (percent / 100.0D);
        if (this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_TAX_ROUNDTOCENTS)) {
            tax = roundCurrency(tax);
        }
        return new TaxBreakdown(gross, tax, percent);
    }

    public void depositTax(double amount) {
        double tax = Math.max(0.0D, amount);
        if (tax <= 0.0D) {
            return;
        }
        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_TAX_DEPOSITTOACCOUNT)) {
            return;
        }
        if (!this.freeframe.getEconomyService().isAvailable()) {
            return;
        }

        String accountName = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_TAX_SERVERACCOUNTNAME);
        if (accountName == null || accountName.trim().isEmpty()) {
            accountName = "server";
        }
        this.freeframe.getEconomyService().depositToOwner("unknown", accountName, tax);
    }

    private static double roundCurrency(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
