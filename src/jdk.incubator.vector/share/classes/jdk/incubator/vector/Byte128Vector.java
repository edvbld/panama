/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */
package jdk.incubator.vector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Byte128Vector extends ByteVector<Shapes.S128Bit> {
    static final Byte128Species SPECIES = new Byte128Species();

    static final Byte128Vector ZERO = new Byte128Vector();

    static final int LENGTH = SPECIES.length();

    private final byte[] vec; // Don't access directly, use getElements() instead.

    private byte[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Byte128Vector() {
        vec = new byte[SPECIES.length()];
    }

    Byte128Vector(byte[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Byte128Vector uOp(FUnOp f) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Byte128Vector(res);
    }

    @Override
    Byte128Vector uOp(Mask<Byte, Shapes.S128Bit> o, FUnOp f) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        boolean[] mbits = ((Byte128Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Byte128Vector(res);
    }

    // Binary operator

    @Override
    Byte128Vector bOp(Vector<Byte, Shapes.S128Bit> o, FBinOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte128Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Byte128Vector(res);
    }

    @Override
    Byte128Vector bOp(Vector<Byte, Shapes.S128Bit> o1, Mask<Byte, Shapes.S128Bit> o2, FBinOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte128Vector)o1).getElements();
        boolean[] mbits = ((Byte128Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Byte128Vector(res);
    }

    // Trinary operator

    @Override
    Byte128Vector tOp(Vector<Byte, Shapes.S128Bit> o1, Vector<Byte, Shapes.S128Bit> o2, FTriOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte128Vector)o1).getElements();
        byte[] vec3 = ((Byte128Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Byte128Vector(res);
    }

    @Override
    Byte128Vector tOp(Vector<Byte, Shapes.S128Bit> o1, Vector<Byte, Shapes.S128Bit> o2, Mask<Byte, Shapes.S128Bit> o3, FTriOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = getElements();
        byte[] vec2 = ((Byte128Vector)o1).getElements();
        byte[] vec3 = ((Byte128Vector)o2).getElements();
        boolean[] mbits = ((Byte128Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Byte128Vector(res);
    }

    @Override
    byte rOp(byte v, FBinOp f) {
        byte[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> add(byte o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> add(byte o, Mask<Byte,Shapes.S128Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> sub(byte o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> sub(byte o, Mask<Byte,Shapes.S128Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> mul(byte o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> mul(byte o, Mask<Byte,Shapes.S128Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> min(byte o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> max(byte o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S128Bit> equal(byte o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S128Bit> notEqual(byte o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S128Bit> lessThan(byte o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S128Bit> lessThanEq(byte o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S128Bit> greaterThan(byte o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S128Bit> greaterThanEq(byte o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> blend(byte o, Mask<Byte,Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> and(byte o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> and(byte o, Mask<Byte,Shapes.S128Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> or(byte o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> or(byte o, Mask<Byte,Shapes.S128Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> xor(byte o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S128Bit> xor(byte o, Mask<Byte,Shapes.S128Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Byte128Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Byte128Vector neg(Mask<Byte, Shapes.S128Bit> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Byte128Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Byte128Vector.class, byte.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (byte) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Byte128Vector abs(Mask<Byte, Shapes.S128Bit> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public Byte128Vector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Byte128Vector.class, byte.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (byte) ~a));
    }

    @ForceInline
    @Override
    public Byte128Vector not(Mask<Byte, Shapes.S128Bit> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public Byte128Vector add(Vector<Byte,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Byte128Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a + b)));
    }

    @Override
    @ForceInline
    public Byte128Vector add(Vector<Byte,Shapes.S128Bit> v, Mask<Byte, Shapes.S128Bit> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Byte128Vector sub(Vector<Byte,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Byte128Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a - b)));
    }

    @Override
    @ForceInline
    public Byte128Vector sub(Vector<Byte,Shapes.S128Bit> v, Mask<Byte, Shapes.S128Bit> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Byte128Vector mul(Vector<Byte,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Byte128Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a * b)));
    }

    @Override
    @ForceInline
    public Byte128Vector mul(Vector<Byte,Shapes.S128Bit> v, Mask<Byte, Shapes.S128Bit> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Byte128Vector min(Vector<Byte,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;
        return (Byte128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Byte128Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte128Vector)v1).bOp(v2, (i, a, b) -> (byte) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Byte128Vector max(Vector<Byte,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Byte128Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte) ((a > b) ? a : b)));
        }

    @Override
    @ForceInline
    public Byte128Vector and(Vector<Byte,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Byte128Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a & b)));
    }

    @Override
    @ForceInline
    public Byte128Vector or(Vector<Byte,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Byte128Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a | b)));
    }

    @Override
    @ForceInline
    public Byte128Vector xor(Vector<Byte,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Byte128Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a ^ b)));
    }

    @Override
    @ForceInline
    public Byte128Vector and(Vector<Byte,Shapes.S128Bit> v, Mask<Byte, Shapes.S128Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Byte128Vector or(Vector<Byte,Shapes.S128Bit> v, Mask<Byte, Shapes.S128Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Byte128Vector xor(Vector<Byte,Shapes.S128Bit> v, Mask<Byte, Shapes.S128Bit> m) {
        return blend(xor(v), m);
    }

    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public byte addAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Byte128Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 0, (i, a, b) -> (byte) (a + b)));
    }

    @Override
    @ForceInline
    public byte andAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Byte128Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) -1, (i, a, b) -> (byte) (a & b)));
    }

    @Override
    @ForceInline
    public byte andAll(Mask<Byte, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((byte) -1), m).andAll();
    }

    @Override
    @ForceInline
    public byte minAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Byte128Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp(Byte.MAX_VALUE , (i, a, b) -> (byte) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public byte maxAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Byte128Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp(Byte.MIN_VALUE , (i, a, b) -> (byte) ((a > b) ? a : b)));
    }

    @Override
    @ForceInline
    public byte mulAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Byte128Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 1, (i, a, b) -> (byte) (a * b)));
    }

    @Override
    @ForceInline
    public byte subAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Byte128Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 0, (i, a, b) -> (byte) (a - b)));
    }

    @Override
    @ForceInline
    public byte orAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Byte128Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 0, (i, a, b) -> (byte) (a | b)));
    }

    @Override
    @ForceInline
    public byte orAll(Mask<Byte, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((byte) 0), m).orAll();
    }

    @Override
    @ForceInline
    public byte xorAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Byte128Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 0, (i, a, b) -> (byte) (a ^ b)));
    }

    @Override
    @ForceInline
    public byte xorAll(Mask<Byte, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((byte) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public byte addAll(Mask<Byte, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((byte) 0), m).addAll();
    }

    @Override
    @ForceInline
    public byte subAll(Mask<Byte, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((byte) 0), m).subAll();
    }

    @Override
    @ForceInline
    public byte mulAll(Mask<Byte, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((byte) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public byte minAll(Mask<Byte, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(Byte.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public byte maxAll(Mask<Byte, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(Byte.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public Shuffle<Byte, Shapes.S128Bit> toShuffle() {
        byte[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return SPECIES.shuffleFromArray(sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_BYTE_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Byte128Vector.class, byte.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(byte[] a, int ax, Mask<Byte, Shapes.S128Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Byte128Vector oldVal = SPECIES.fromArray(a, ax);
        Byte128Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        // @@@ Endianess
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Byte128Vector.class, byte.class, LENGTH,
                               a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                   ByteBuffer tb = bbc;
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int ix, Mask<Byte, Shapes.S128Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Byte128Vector oldVal = SPECIES.fromByteArray(a, ix);
        Byte128Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        // @@@ Endianess
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        if (bb.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Byte128Vector.class, byte.class, LENGTH,
                               U.getObject(bb, BYTE_BUFFER_HB), ix + U.getLong(bb, BUFFER_ADDRESS),
                               this,
                               bb, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                   ByteBuffer tb = bbc;
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Byte, Shapes.S128Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Byte128Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Byte128Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, ix);
    }

    //

    @Override
    public String toString() {
        return Arrays.toString(getElements());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        // @@@ Use equal op
        Byte128Vector that = (Byte128Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Byte128Mask bTest(Vector<Byte, Shapes.S128Bit> o, FBinTest f) {
        byte[] vec1 = getElements();
        byte[] vec2 = ((Byte128Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Byte128Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Byte128Mask equal(Vector<Byte, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Byte128Vector.class, Byte128Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Byte128Mask notEqual(Vector<Byte, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Byte128Vector.class, Byte128Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Byte128Mask lessThan(Vector<Byte, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Byte128Vector.class, Byte128Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Byte128Mask lessThanEq(Vector<Byte, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Byte128Vector.class, Byte128Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Byte128Mask greaterThan(Vector<Byte, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;

        return (Byte128Mask) VectorIntrinsics.compare(
            BT_gt, Byte128Vector.class, Byte128Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Byte128Mask greaterThanEq(Vector<Byte, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Byte128Vector v = (Byte128Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Byte128Vector.class, Byte128Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        byte[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Byte, Shapes.S128Bit> o, FUnCon f) {
        boolean[] mbits = ((Byte128Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Byte128Vector rotateEL(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Byte128Vector(res);
    }

    @Override
    public Byte128Vector rotateER(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Byte128Vector(res);
    }

    @Override
    public Byte128Vector shiftEL(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Byte128Vector(res);
    }

    @Override
    public Byte128Vector shiftER(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Byte128Vector(res);
    }

    @Override
    @ForceInline
    public Byte128Vector rearrange(Vector<Byte, Shapes.S128Bit> v,
                                  Shuffle<Byte, Shapes.S128Bit> s, Mask<Byte, Shapes.S128Bit> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Byte128Vector rearrange(Shuffle<Byte, Shapes.S128Bit> o1) {
    Objects.requireNonNull(o1);
    Byte128Shuffle s =  (Byte128Shuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            Byte128Vector.class, Byte128Shuffle.class, byte.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
            byte[] vec = this.getElements();
            int ei = s_.getElement(i);
            return vec[ei];
        }));
    }

    @Override
    @ForceInline
    public Byte128Vector blend(Vector<Byte, Shapes.S128Bit> o1, Mask<Byte, Shapes.S128Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Byte128Vector v = (Byte128Vector)o1;
        Byte128Mask   m = (Byte128Mask)o2;

        return VectorIntrinsics.blend(
            Byte128Vector.class, Byte128Mask.class, byte.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public byte get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return (byte) VectorIntrinsics.extract(
                                Byte128Vector.class, byte.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    byte[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Byte128Vector with(int i, byte e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Byte128Vector.class, byte.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    byte[] res = v.getElements().clone();
                                    res[ix] = (byte)bits;
                                    return new Byte128Vector(res);
                                });
    }

    // Mask

    static final class Byte128Mask extends AbstractMask<Byte, Shapes.S128Bit> {
        static final Byte128Mask TRUE_MASK = new Byte128Mask(true);
        static final Byte128Mask FALSE_MASK = new Byte128Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Byte128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Byte128Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Byte128Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Byte128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Byte128Mask(res);
        }

        @Override
        Byte128Mask bOp(Mask<Byte, Shapes.S128Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Byte128Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Byte128Mask(res);
        }

        @Override
        public Byte128Species species() {
            return SPECIES;
        }

        @Override
        public Byte128Vector toVector() {
            byte[] res = new byte[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (byte) (bits[i] ? -1 : 0);
            }
            return new Byte128Vector(res);
        }

        // Unary operations

        @Override
        @ForceInline
        public Byte128Mask not() {
            return (Byte128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Byte128Mask.class, byte.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Byte128Mask and(Mask<Byte,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Byte128Mask m = (Byte128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Byte128Mask.class, byte.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Byte128Mask or(Mask<Byte,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Byte128Mask m = (Byte128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Byte128Mask.class, byte.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Byte128Mask.class, byte.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Byte128Mask.class, byte.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Byte128Shuffle extends AbstractShuffle<Byte, Shapes.S128Bit> {
        Byte128Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Byte128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Byte128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Byte128Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public Byte128Species species() {
            return SPECIES;
        }

        @Override
        public Byte128Vector toVector() {
            byte[] va = new byte[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (byte) getElement(i);
            }
            return species().fromArray(va, 0);
        }

        @Override
        public Byte128Shuffle rearrange(Vector.Shuffle<Byte, Shapes.S128Bit> o) {
            Byte128Shuffle s = (Byte128Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Byte128Shuffle(r);
        }
    }

    // Species

    @Override
    public Byte128Species species() {
        return SPECIES;
    }

    static final class Byte128Species extends ByteSpecies<Shapes.S128Bit> {
        static final int BIT_SIZE = Shapes.S_128_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Byte.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(byte.class.getSimpleName()).append("s x ");
           sb.append(elementSize()).append(" bits");
           sb.append("]");
           return sb.toString();
        }

        @Override
        @ForceInline
        public int bitSize() {
            return BIT_SIZE;
        }

        @Override
        @ForceInline
        public int length() {
            return LENGTH;
        }

        @Override
        @ForceInline
        public Class<Byte> elementType() {
            return byte.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Byte.SIZE;
        }

        @Override
        @ForceInline
        public Shapes.S128Bit shape() {
            return Shapes.S_128_BIT;
        }

        @Override
        Byte128Vector op(FOp f) {
            byte[] res = new byte[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Byte128Vector(res);
        }

        @Override
        Byte128Vector op(Mask<Byte, Shapes.S128Bit> o, FOp f) {
            byte[] res = new byte[length()];
            boolean[] mbits = ((Byte128Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Byte128Vector(res);
        }

        // Factories

        @Override
        public Byte128Mask maskFromValues(boolean... bits) {
            return new Byte128Mask(bits);
        }

        @Override
        public Byte128Mask maskFromArray(boolean[] bits, int i) {
            return new Byte128Mask(bits, i);
        }

        @Override
        public Byte128Shuffle shuffle(IntUnaryOperator f) {
            return new Byte128Shuffle(f);
        }

        @Override
        public Byte128Shuffle shuffleIota() {
            return new Byte128Shuffle(AbstractShuffle.IDENTITY);
        }

        @Override
        public Byte128Shuffle shuffleFromValues(int... ixs) {
            return new Byte128Shuffle(ixs);
        }

        @Override
        public Byte128Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Byte128Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Byte128Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Byte128Vector.class, byte.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Byte128Vector broadcast(byte e) {
            return VectorIntrinsics.broadcastCoerced(
                Byte128Vector.class, byte.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (byte)bits)));
        }

        @Override
        @ForceInline
        public Byte128Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Byte128Mask.class, byte.class, LENGTH,
                                                     (byte)-1,
                                                     (z -> Byte128Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Byte128Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Byte128Mask.class, byte.class, LENGTH,
                                                     0,
                                                     (z -> Byte128Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Byte128Vector scalars(byte... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return VectorIntrinsics.load(Byte128Vector.class, byte.class, LENGTH,
                                         es, Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         es, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Byte128Vector fromArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return VectorIntrinsics.load(Byte128Vector.class, byte.class, LENGTH,
                                         a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Byte128Vector fromArray(byte[] a, int ax, Mask<Byte, Shapes.S128Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromArray(a, ax), m);
        }

        @Override
        @ForceInline
        public Byte128Vector fromByteArray(byte[] a, int ix) {
            // @@@ Endianess
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Byte128Vector.class, byte.class, LENGTH,
                                         a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                             ByteBuffer tb = bbc;
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Byte128Vector fromByteArray(byte[] a, int ix, Mask<Byte, Shapes.S128Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Byte128Vector fromByteBuffer(ByteBuffer bb, int ix) {
            // @@@ Endianess
            if (bb.order() != ByteOrder.nativeOrder()) {
                throw new IllegalArgumentException();
            }
            ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Byte128Vector.class, byte.class, LENGTH,
                                         U.getObject(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                         bb, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                             ByteBuffer tb = bbc;
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Byte128Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Byte, Shapes.S128Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> Byte128Vector cast(Vector<F, T> o) {
            if (o.length() != LENGTH)
                throw new IllegalArgumentException("Vector length this species length differ");

            return VectorIntrinsics.cast(
                o.getClass(),
                o.elementType(), LENGTH,
                byte.class, LENGTH,
                o, this,
                (s, v) -> s.castDefault(v)
            );
        }

        @SuppressWarnings("unchecked")
        @ForceInline
        private <F, T extends Shape> Byte128Vector castDefault(Vector<F, T> v) {
            // Allocate array of required size
            int limit = length();
            byte[] a = new byte[limit];

            Class<?> vtype = v.species().elementType();
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Byte128Mask cast(Mask<E, S> m) {
            if (m.length() != LENGTH)
                throw new IllegalArgumentException("Mask length this species length differ");
            return new Byte128Mask(m.toArray());
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Byte128Shuffle cast(Shuffle<E, S> s) {
            if (s.length() != LENGTH)
                throw new IllegalArgumentException("Shuffle length this species length differ");
            return new Byte128Shuffle(s.toArray());
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> Byte128Vector rebracket(Vector<F, Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte128Vector.class,
                    byte.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short128Vector.class,
                    short.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int128Vector.class,
                    int.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long128Vector.class,
                    long.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float128Vector.class,
                    float.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double128Vector.class,
                    double.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Byte128Vector resize(Vector<Byte, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte64Vector.class,
                    byte.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte128Vector.class,
                    byte.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte256Vector.class,
                    byte.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte512Vector.class,
                    byte.class, so.length(),
                    byte.class, LENGTH,
                    so, this,
                    (s, v) -> (Byte128Vector) s.reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}