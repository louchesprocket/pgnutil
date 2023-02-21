package com.dotfx.pgnutil;

public final class NormalizedRound implements Comparable<NormalizedRound>
{
    private final String round;
    private final Integer parts[];

    public NormalizedRound(String round)
    {
        if (round == null) throw new IllegalArgumentException("round may not be null");

        if (round.equals("-") || round.equals("?"))
        {
            this.round = round;
            parts = null;
            return;
        }

        String tokens[] = round.split("\\.", -1);

        if (CLOptions.aquarium && tokens.length == 1) // fix Aquarium stupidity
        {
            Integer n = null;

            try { n = Integer.parseInt(round); } // stupid workaround for i.d.e.
            catch (NumberFormatException e) {}

            if (n != null)
            {
                parts = new Integer[1];

                if (n < 1)
                {
                    this.round = String.valueOf(n + 128);
                    parts[0] = n + 128;
                }

                else
                {
                    this.round = round;
                    parts[0] = n;
                }

                return;
            }
        }

        parts = new Integer[tokens.length];

        try { for (int i = 0; i < tokens.length; i++) parts[i] = Integer.parseInt(tokens[i]); }

        catch (NumberFormatException e)
        {
//            if (!round.matches("^[0-9]+(\\.[0-9]+)*$") && !round.equals("-") && !round.equals("?"))
                throw new IllegalArgumentException("invalid round value '" + round + "'");
        }

        this.round = round;
    }

    public String get() { return this.round; }
    public int ordinalValue() { return parts == null || parts.length != 1 ? -1 : parts[0]; }

    public boolean canFollow(NormalizedRound other)
    {
        if (parts == null || other.parts == null || parts.length != other.parts.length) return false;

        for (int i = 0; i < parts.length; i++)
        {
            if (parts[i] < 1 || other.parts[i] < 1) return false;
            int diff = parts[i] - other.parts[i];

            if (diff == 1) // part has been incremented
            {
                for (int j = i + 1; j < parts.length; j++) if (parts[j] != 1) return false;
                return true;
            }

            if (diff != 0) return false;
        }

        return false;
    }

    @Override
    public int compareTo(NormalizedRound that)
    {
        if (that == null) return 1;
        boolean amNoRound = round.equals("-");
        boolean thatIsNoRound = that.round.equals("-");
        boolean amUnknown = round.equals("?");
        boolean thatIsUnknown = that.round.equals("?");

        if (amNoRound)
        {
            if (!thatIsNoRound) return -1;
            return 0;
        }

        if (thatIsNoRound) return 1;

        if (amUnknown)
        {
            if (!thatIsUnknown) return -1;
            return 0;
        }

        if (thatIsUnknown) return 1;

        // parts cannot be null at this point

        for (int i = 0; i < parts.length; i++)
        {
            if (that.parts.length <= i) return 1;
            int diff = parts[i] - that.parts[i];
            if (diff != 0) return diff;
        }

        if (that.parts.length > parts.length) return -1;
        return 0;
    }

    @Override
    public boolean equals(Object that)
    {
        try { return compareTo((NormalizedRound)that) == 0; }
        catch (NullPointerException | ClassCastException e) { return false; }
    }

    @Override
    public int hashCode()
    {
        int ret = NormalizedRound.class.hashCode();
        for (int i = 0; i < parts.length; i++) ret ^= parts[i].hashCode();
        return ret;
    }

    @Override public String toString() { return round; }

//    public static void main(String args[]) throws Exception
//    {
//        System.out.println(new NormalizedRound("1.1").canFollow(new NormalizedRound(("1.1"))));
//        System.out.println(new NormalizedRound("1.0").canFollow(new NormalizedRound(("1.1"))));
//        System.out.println(new NormalizedRound("1.1.1").canFollow(new NormalizedRound(("1.1"))));
//        System.out.println(new NormalizedRound("1.2").canFollow(new NormalizedRound(("1.1"))));
////        TreeSet<NormalizedRound> t = new TreeSet<>();
//////        t.add(new NormalizedRound("1."));
//////        t.add(new NormalizedRound("-1.3"));
//////        t.add(new NormalizedRound("2.-54"));
//////        t.add(new NormalizedRound("0."));
////        t.add(new NormalizedRound("1"));
////        t.add(new NormalizedRound("?"));
//////        t.add(new NormalizedRound("-8"));
////        t.add(new NormalizedRound("6"));
////        t.add(new NormalizedRound("0"));
////        t.add(new NormalizedRound("0.6"));
////        t.add(new NormalizedRound("?"));
////        t.add(new NormalizedRound("1"));
////        t.add(new NormalizedRound("-"));
////        t.add(new NormalizedRound("6"));
////        t.add(new NormalizedRound("0.6.2"));
////        t.add(new NormalizedRound("-"));
////        t.add(new NormalizedRound("1.0.1"));
////        t.add(new NormalizedRound("01.1"));
////        t.add(new NormalizedRound("1.0"));
////
////        for (NormalizedRound r: t) System.out.println(r);
//    }
}