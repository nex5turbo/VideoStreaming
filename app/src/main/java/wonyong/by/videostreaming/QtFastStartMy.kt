package wonyong.by.videostreaming

import android.util.Log
import android.widget.Toast

import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class QtFastStartMy {
    val FREE_ATOM = fourCcToInt(byteArrayOf('f'.toByte(), 'r'.toByte(), 'e'.toByte(), 'e'.toByte()))
    val JUNK_ATOM = fourCcToInt(byteArrayOf('j'.toByte(), 'u'.toByte(), 'n'.toByte(), 'k'.toByte()))
    val MDAT_ATOM = fourCcToInt(byteArrayOf('m'.toByte(), 'd'.toByte(), 'a'.toByte(), 't'.toByte()))
    val MOOV_ATOM = fourCcToInt(byteArrayOf('m'.toByte(), 'o'.toByte(), 'o'.toByte(), 'v'.toByte()))
    val PNOT_ATOM = fourCcToInt(byteArrayOf('p'.toByte(), 'n'.toByte(), 'o'.toByte(), 't'.toByte()))
    val SKIP_ATOM = fourCcToInt(byteArrayOf('s'.toByte(), 'k'.toByte(), 'i'.toByte(), 'p'.toByte()))
    val WIDE_ATOM = fourCcToInt(byteArrayOf('w'.toByte(), 'i'.toByte(), 'd'.toByte(), 'e'.toByte()))
    val PICT_ATOM = fourCcToInt(byteArrayOf('P'.toByte(), 'I'.toByte(), 'C'.toByte(), 'T'.toByte()))
    val FTYP_ATOM = fourCcToInt(byteArrayOf('f'.toByte(), 't'.toByte(), 'y'.toByte(), 'p'.toByte()))
    val UUID_ATOM = fourCcToInt(byteArrayOf('u'.toByte(), 'u'.toByte(), 'i'.toByte(), 'd'.toByte()))

    val CMOV_ATOM = fourCcToInt(byteArrayOf('c'.toByte(), 'm'.toByte(), 'o'.toByte(), 'v'.toByte()))
    val STCO_ATOM = fourCcToInt(byteArrayOf('s'.toByte(), 't'.toByte(), 'c'.toByte(), 'o'.toByte()))
    val CO64_ATOM = fourCcToInt(byteArrayOf('c'.toByte(), 'o'.toByte(), '6'.toByte(), '4'.toByte()))

    private val ATOM_PREAMBLE_SIZE = 8


    private fun safeClose(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("###", "Failed to close file: ")
            }

        }
    }

    internal fun uint32ToLong(int32: Int): Long {
        var ret = int32.toLong() and 0x00000000ffffffffL
        return ret
    }

    internal fun uint32ToInt(uint32: Int): Int {
        if (uint32 < 0) {
            Log.d("###", "uint32 value is too large")
        }
        return uint32
    }

    internal fun uint32ToInt(uint32: Long): Int {
        if (uint32 > Integer.MAX_VALUE || uint32 < 0) {
            Log.d("###", "uint32 value is too large")
        }
        return uint32.toInt()
    }

    internal fun uint64ToLong(uint64: Long): Long {
        if (uint64 < 0) Log.d("###", "uint64 value is too large")
        return uint64
    }

    private fun fourCcToInt(byteArray: ByteArray): Int {
        return ByteBuffer.wrap(byteArray).order(ByteOrder.BIG_ENDIAN).int
    }

    private fun readAndFill(infile: FileChannel, buffer: ByteBuffer): Boolean {
        buffer.clear()
        val size = infile.read(buffer)
        buffer.flip()
        return size == buffer.capacity()
    }

    private fun readAndFill(infile: FileChannel, buffer: ByteBuffer, position: Long): Boolean {
        buffer.clear()
        val size = infile.read(buffer, position)
        buffer.flip()
        return size == buffer.capacity()
    }

    fun fastStart(infile: File, outfile: File, context: ServerActivity): Boolean {
        var ret = false
        var inStream: FileInputStream? = null
        var outStream: FileOutputStream? = null
        try {
            inStream = FileInputStream(infile)
            val infile = inStream.channel
            outStream = FileOutputStream(outfile)
            val outfile = outStream.channel
            ret = fastStartImpl(infile, outfile)
            context.runOnUiThread(object : Runnable{
                override fun run() {
                    Toast.makeText(context, "파일 변환완료...", Toast.LENGTH_SHORT).show()
                }
            })
            return ret
        } finally {
            safeClose(inStream)
            safeClose(outStream)
            if (!ret) {
                outfile.delete()
            }
        }
    }

    private fun fastStartImpl(infile: FileChannel, outfile: FileChannel): Boolean {
        val atomBytes = ByteBuffer.allocate(ATOM_PREAMBLE_SIZE).order(ByteOrder.BIG_ENDIAN)
        var atomType = 0
        var atomSize: Long = 0 // uint64_t
        val lastOffset: Long
        val moovAtom: ByteBuffer
        var ftypAtom: ByteBuffer? = null
        // uint64_t, but assuming it is in int32 range. It is reasonable as int max is around 2GB. Such large moov is unlikely, yet unallocatable :).
        val moovAtomSize: Int
        var startOffset: Long = 0

        // traverse through the atoms in the file to make sure that 'moov' is at the end
        while (readAndFill(infile, atomBytes)) {
            atomSize = uint32ToLong(atomBytes.int) // uint32
            atomType = atomBytes.int // representing uint32_t in signed int

            // keep ftyp atom
            if (atomType == FTYP_ATOM) {
                val ftypAtomSize = uint32ToInt(atomSize) // XXX: assume in range of int32_t
                ftypAtom = ByteBuffer.allocate(ftypAtomSize).order(ByteOrder.BIG_ENDIAN)
                atomBytes.rewind()
                ftypAtom!!.put(atomBytes)
                if (infile.read(ftypAtom) < ftypAtomSize - ATOM_PREAMBLE_SIZE) break
                ftypAtom.flip()
                startOffset = infile.position() // after ftyp atom
            } else {
                if (atomSize == 1L) {
                    /* 64-bit special case */
                    atomBytes.clear()
                    if (!readAndFill(infile, atomBytes)) break
                    atomSize = uint64ToLong(atomBytes.long) // XXX: assume in range of int64_t
                    infile.position(infile.position() + atomSize - ATOM_PREAMBLE_SIZE * 2) // seek
                } else {
                    infile.position(infile.position() + atomSize - ATOM_PREAMBLE_SIZE) // seek
                }
            }

            if (atomType != FREE_ATOM
                && atomType != JUNK_ATOM
                && atomType != MDAT_ATOM
                && atomType != MOOV_ATOM
                && atomType != PNOT_ATOM
                && atomType != SKIP_ATOM
                && atomType != WIDE_ATOM
                && atomType != PICT_ATOM
                && atomType != UUID_ATOM
                && atomType != FTYP_ATOM
            ) {
                Log.d("###","encountered non-QT top-level atom (is this a QuickTime file?)")
                break
            }

            /* The atom header is 8 (or 16 bytes), if the atom size (which
             * includes these 8 or 16 bytes) is less than that, we won't be
             * able to continue scanning sensibly after this atom, so break. */
            if (atomSize < 8)
                break
        }

        if (atomType != MOOV_ATOM) {
            Log.d("###", "last atom in file was not a moov atom")
            return false
        }

        // moov atom was, in fact, the last atom in the chunk; load the whole moov atom

        // atomSize is uint64, but for moov uint32 should be stored.
        // XXX: assuming moov atomSize <= max vaue of int32
        moovAtomSize = uint32ToInt(atomSize)
        lastOffset = infile.size() - moovAtomSize // NOTE: assuming no extra data after moov, as qt-faststart.c
        moovAtom = ByteBuffer.allocate(moovAtomSize).order(ByteOrder.BIG_ENDIAN)
        if (!readAndFill(infile, moovAtom, lastOffset)) {
            Log.d("###", "failed to read moov atom")
        }

        // this utility does not support compressed atoms yet, so disqualify files with compressed QT atoms
        if (moovAtom.getInt(12) == CMOV_ATOM) {
            Log.d("###", "this utility does not support compressed moov atoms yet")
        }

        // crawl through the moov chunk in search of stco or co64 atoms
        while (moovAtom.remaining() >= 8) {
            val atomHead = moovAtom.position()
            atomType = moovAtom.getInt(atomHead + 4) // representing uint32_t in signed int
            if (!(atomType == STCO_ATOM || atomType == CO64_ATOM)) {
                moovAtom.position(moovAtom.position() + 1)
                continue
            }
            atomSize = uint32ToLong(moovAtom.getInt(atomHead)) // uint32
            if (atomSize > moovAtom.remaining()) {
                Log.d("###", "bad atom size")
            }
            moovAtom.position(atomHead + 12) // skip size (4 bytes), type (4 bytes), version (1 byte) and flags (3 bytes)
            if (moovAtom.remaining() < 4) {
                Log.d("###", "malformed atom")
            }
            // uint32_t, but assuming moovAtomSize is in int32 range, so this will be in int32 range
            val offsetCount = uint32ToInt(moovAtom.int)
            if (atomType == STCO_ATOM) {
                Log.d("###", "patching stco atom...")
                if (moovAtom.remaining() < offsetCount * 4) {
                    Log.d("###", "bad atom size/element count")
                }
                for (i in 0 until offsetCount) {
                    val currentOffset = moovAtom.getInt(moovAtom.position())
                    val newOffset = currentOffset + moovAtomSize // calculate uint32 in int, bitwise addition
                    // current 0xffffffff => new 0x00000000 (actual >= 0x0000000100000000L)
                    if (currentOffset < 0 && newOffset >= 0) {
                        Log.d(
                            "###", "This is bug in original qt-faststart.c: "
                                    + "stco atom should be extended to co64 atom as new offset value overflows uint32, "
                                    + "but is not implemented."
                        )
                    }
                    moovAtom.putInt(newOffset)
                }
            } else if (atomType == CO64_ATOM) {
                Log.d("###", "patching co64 atom...")
                if (moovAtom.remaining() < offsetCount * 8) {
                    Log.d("###", "bad atom size/element count")
                }
                for (i in 0 until offsetCount) {
                    val currentOffset = moovAtom.getLong(moovAtom.position())
                    moovAtom.putLong(currentOffset + moovAtomSize) // calculate uint64 in long, bitwise addition
                }
            }
        }

        infile.position(startOffset) // seek after ftyp atom

        if (ftypAtom != null) {
            // dump the same ftyp atom
            Log.d("###", "writing ftyp atom...")
            ftypAtom.rewind()
            outfile.write(ftypAtom)
        }

        // dump the new moov atom
        Log.d("###","writing moov atom...")
        moovAtom.rewind()
        outfile.write(moovAtom)

        // copy the remainder of the infile, from offset 0 -> (lastOffset - startOffset) - 1
        Log.d("###", "copying rest of file...")
        infile.transferTo(startOffset, lastOffset - startOffset, outfile)

        return true
    }

}