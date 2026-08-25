namespace code;

public class Preisrechner
{
    public double BerechnePreis(double grundpreis, double sonderpreis, double zubehoerpreis, int anzahlZubehoer, double haendlerrabatt)
    {
        double zubehoerRabatt;

        if (anzahlZubehoer >= 5)
            zubehoerRabatt = 15;
        else if (anzahlZubehoer >= 3)
            zubehoerRabatt = 10;
        else
            zubehoerRabatt = 0;

        if (haendlerrabatt > zubehoerRabatt)
            zubehoerRabatt = haendlerrabatt;

        double preis = grundpreis / 100.0 * (100 - haendlerrabatt) + sonderpreis
                        + zubehoerpreis / 100.0 * (100 - zubehoerRabatt);

        return preis;
    }
}
