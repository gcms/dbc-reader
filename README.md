Provides `InputStream` implementation for reading data compressed in PKWare's Data Compression Library (DCL).

Two main classes are provided:

- `BlastInputStream` can be used to decompress data compressed using PkWare DCL
- `DBCInputStream` can be used to decompress DBC files to DBF (xBase database file format). This format is used by Brazilian Health Ministry information agency [DATASUS](http://datasus.saude.gov.br/) for releasing public healthcare data.



This work is based on the following sources, refactoring it to an object-oriented design and to work as a Java `InputStream`.

- Blast decompressor by Mark Adler madler@alumni.caltech.edu https://github.com/madler/zlib/tree/master/contrib/blast
- Direct translation of the blast decompressor to Java  https://github.com/aminea7/GraphicalUserInterface/blob/master/mpxj/src/main/java/net/sf/mpxj/primavera/common/Blast.java