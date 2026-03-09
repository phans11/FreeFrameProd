package de.isolveproblems.freeframe.utils;

public class TaxBreakdown {
    private final double gross;
    private final double tax;
    private final double net;
    private final double percent;

    public TaxBreakdown(double gross, double tax, double percent) {
        this.gross = Math.max(0.0D, gross);
        this.tax = Math.max(0.0D, Math.min(this.gross, tax));
        this.net = Math.max(0.0D, this.gross - this.tax);
        this.percent = Math.max(0.0D, Math.min(100.0D, percent));
    }

    public double getGross() {
        return this.gross;
    }

    public double getTax() {
        return this.tax;
    }

    public double getNet() {
        return this.net;
    }

    public double getPercent() {
        return this.percent;
    }
}
