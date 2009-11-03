Properties used in band descriptors:

    Name            Type            Explanation
    
    visible         boolean         should the band appear in the BEAM product tree? (not used)
    band            string          band name used in BEAM
    dataset         string          the name of the dataset
    index           int             index in sequence
    sampleModel     int             0, 1, 2
    scalingOffset   double          scaling offset applied to the raw data
    scalingFactor   double          scaling factor applied to the raw data
    typicalMin      double          typical (scaled) minimum value
    typicalMax      double          typical (scaled) maximum value
    cyclic          boolean         is the value range cyclic?
    fillValue       double          value indicating no-data in BEAM
    validExpr       string          valid-pixel expression used in BEAM
    unit            string          unit
    description     string          description
    flagCoding      string          for flag bands only: name of the flag coding
    flags           string          for flag bands only: name of the flag descriptor resource

When you do not know the value of a property or do not care about the value use an asterisk '*'.
A hash mark '#' can be used to commence comments. Use UTF-8 character encoding.

