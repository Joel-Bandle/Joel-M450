using code;

bool TestBerechnePreis()
{
    var rechner = new Preisrechner();
    bool alleTestsOk = true;

    (double grundpreis, double sonderpreis, double zubehoerpreis, int anzahlZubehoer, double haendlerrabatt, double soll)[] faelle =
    {
        (1000, 200, 500, 1, 5, 1625),
        (1000, 200, 500, 3, 5, 1600),
        (1000, 200, 500, 5, 5, 1575),
        (1000, 200, 500, 6, 20, 1400),
    };

    foreach (var fall in faelle)
    {
        double preis = rechner.BerechnePreis(fall.grundpreis, fall.sonderpreis, fall.zubehoerpreis, fall.anzahlZubehoer, fall.haendlerrabatt);
        bool testOk = Math.Abs(preis - fall.soll) < 0.001;
        alleTestsOk &= testOk;

        Console.WriteLine($"extras={fall.anzahlZubehoer}, rabatt={fall.haendlerrabatt}%: erwartet={fall.soll}, erhalten={preis} -> {(testOk ? "OK" : "FEHLER")}");
    }

    return alleTestsOk;
}

bool ergebnis = TestBerechnePreis();
Console.WriteLine(ergebnis ? "Alle Tests bestanden" : "Mindestens ein Test fehlgeschlagen");
