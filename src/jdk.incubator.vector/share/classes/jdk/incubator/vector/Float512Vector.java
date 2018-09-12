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
import java.nio.FloatBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Float512Vector extends FloatVector<Shapes.S512Bit> {
    static final Float512Species SPECIES = new Float512Species();

    static final Float512Vector ZERO = new Float512Vector();

    static final int LENGTH = SPECIES.length();

    private final float[] vec; // Don't access directly, use getElements() instead.

    private float[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Float512Vector() {
        vec = new float[SPECIES.length()];
    }

    Float512Vector(float[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Float512Vector uOp(FUnOp f) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Float512Vector(res);
    }

    @Override
    Float512Vector uOp(Mask<Float, Shapes.S512Bit> o, FUnOp f) {
        float[] vec = getElements();
        float[] res = new float[length()];
        boolean[] mbits = ((Float512Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Float512Vector(res);
    }

    // Binary operator

    @Override
    Float512Vector bOp(Vector<Float, Shapes.S512Bit> o, FBinOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float512Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Float512Vector(res);
    }

    @Override
    Float512Vector bOp(Vector<Float, Shapes.S512Bit> o1, Mask<Float, Shapes.S512Bit> o2, FBinOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float512Vector)o1).getElements();
        boolean[] mbits = ((Float512Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Float512Vector(res);
    }

    // Trinary operator

    @Override
    Float512Vector tOp(Vector<Float, Shapes.S512Bit> o1, Vector<Float, Shapes.S512Bit> o2, FTriOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float512Vector)o1).getElements();
        float[] vec3 = ((Float512Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Float512Vector(res);
    }

    @Override
    Float512Vector tOp(Vector<Float, Shapes.S512Bit> o1, Vector<Float, Shapes.S512Bit> o2, Mask<Float, Shapes.S512Bit> o3, FTriOp f) {
        float[] res = new float[length()];
        float[] vec1 = getElements();
        float[] vec2 = ((Float512Vector)o1).getElements();
        float[] vec3 = ((Float512Vector)o2).getElements();
        boolean[] mbits = ((Float512Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Float512Vector(res);
    }

    @Override
    float rOp(float v, FBinOp f) {
        float[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> add(float o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> add(float o, Mask<Float,Shapes.S512Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> sub(float o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> sub(float o, Mask<Float,Shapes.S512Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> mul(float o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> mul(float o, Mask<Float,Shapes.S512Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> min(float o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> max(float o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S512Bit> equal(float o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S512Bit> notEqual(float o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S512Bit> lessThan(float o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S512Bit> lessThanEq(float o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S512Bit> greaterThan(float o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S512Bit> greaterThanEq(float o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> blend(float o, Mask<Float,Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> div(float o) {
        return div(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> div(float o, Mask<Float,Shapes.S512Bit> m) {
        return div(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Float512Vector div(Vector<Float,Shapes.S512Bit> v, Mask<Float, Shapes.S512Bit> m) {
        return blend(div(v), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> atan2(float o) {
        return atan2(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> atan2(float o, Mask<Float,Shapes.S512Bit> m) {
        return atan2(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> pow(float o) {
        return pow(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> pow(float o, Mask<Float,Shapes.S512Bit> m) {
        return pow(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> fma(float o1, float o2) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> fma(float o1, float o2, Mask<Float,Shapes.S512Bit> m) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> hypot(float o) {
        return hypot(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S512Bit> hypot(float o, Mask<Float,Shapes.S512Bit> m) {
        return hypot(SPECIES.broadcast(o), m);
    }


    // Unary operations

    @ForceInline
    @Override
    public Float512Vector neg(Mask<Float, Shapes.S512Bit> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Float512Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (float) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Float512Vector abs(Mask<Float, Shapes.S512Bit> m) {
        return blend(abs(), m);
    }

    @Override
    @ForceInline
    public Float512Vector neg() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NEG, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (float) -a));
    }

    @Override
    @ForceInline
    public Float512Vector div(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (float)(a / b)));
    }

    @Override
    @ForceInline
    public Float512Vector sqrt() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_SQRT, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (float) Math.sqrt((double) a)));
    }

    @Override
    @ForceInline
    public Float512Vector exp() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_EXP, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.exp((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector log1p() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_LOG1P, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.log1p((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector log() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_LOG, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.log((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector log10() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_LOG10, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.log10((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector expm1() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_EXPM1, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.expm1((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector cbrt() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_CBRT, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.cbrt((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector sin() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SIN, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.sin((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector cos() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_COS, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.cos((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector tan() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_TAN, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.tan((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector asin() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ASIN, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.asin((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector acos() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ACOS, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.acos((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector atan() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ATAN, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.atan((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector sinh() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SINH, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.sinh((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector cosh() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_COSH, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.cosh((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector tanh() {
        return (Float512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_TANH, Float512Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float512Vector)v1).uOp((i, a) -> (float) Math.tanh((double) a)));
    }
    
    @Override
    @ForceInline
    public Float512Vector pow(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return (Float512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_POW, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float512Vector)v1).bOp(v2, (i, a, b) -> (float)(Math.pow(a,b))));
    }
    
    @Override
    @ForceInline
    public Float512Vector hypot(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return (Float512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_HYPOT, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float512Vector)v1).bOp(v2, (i, a, b) -> (float)(Math.hypot(a,b))));
    }
    
    @Override
    @ForceInline
    public Float512Vector atan2(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return (Float512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ATAN2, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float512Vector)v1).bOp(v2, (i, a, b) -> (float)(Math.atan2(a,b))));
    }
    

    // Binary operations

    @Override
    @ForceInline
    public Float512Vector add(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (float)(a + b)));
    }

    @Override
    @ForceInline
    public Float512Vector add(Vector<Float,Shapes.S512Bit> v, Mask<Float, Shapes.S512Bit> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Float512Vector sub(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (float)(a - b)));
    }

    @Override
    @ForceInline
    public Float512Vector sub(Vector<Float,Shapes.S512Bit> v, Mask<Float, Shapes.S512Bit> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Float512Vector mul(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (float)(a * b)));
    }

    @Override
    @ForceInline
    public Float512Vector mul(Vector<Float,Shapes.S512Bit> v, Mask<Float, Shapes.S512Bit> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Float512Vector min(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return (Float512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float512Vector)v1).bOp(v2, (i, a, b) -> (float) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Float512Vector max(Vector<Float,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Float512Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (float) ((a > b) ? a : b)));
        }


    // Ternary operations

    @Override
    @ForceInline
    public Float512Vector fma(Vector<Float,Shapes.S512Bit> o1, Vector<Float,Shapes.S512Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Float512Vector v1 = (Float512Vector)o1;
        Float512Vector v2 = (Float512Vector)o2;
        return VectorIntrinsics.ternaryOp(
            VECTOR_OP_FMA, Float512Vector.class, float.class, LENGTH,
            this, v1, v2,
            (w1, w2, w3) -> w1.tOp(w2, w3, (i, a, b, c) -> Math.fma(a, b, c)));
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public float addAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_ADD, Float512Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp((float) 0, (i, a, b) -> (float) (a + b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    @ForceInline
    public float subAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_SUB, Float512Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp((float) 0, (i, a, b) -> (float) (a - b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    @ForceInline
    public float mulAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MUL, Float512Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp((float) 1, (i, a, b) -> (float) (a * b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    @ForceInline
    public float minAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MIN, Float512Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp(Float.MAX_VALUE , (i, a, b) -> (float) ((a < b) ? a : b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    @ForceInline
    public float maxAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MAX, Float512Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp(Float.MIN_VALUE , (i, a, b) -> (float) ((a > b) ? a : b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }


    @Override
    @ForceInline
    public float addAll(Mask<Float, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((float) 0), m).addAll();
    }

    @Override
    @ForceInline
    public float subAll(Mask<Float, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((float) 0), m).subAll();
    }

    @Override
    @ForceInline
    public float mulAll(Mask<Float, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((float) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public float minAll(Mask<Float, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(Float.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public float maxAll(Mask<Float, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(Float.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public Shuffle<Float, Shapes.S512Bit> toShuffle() {
        float[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return SPECIES.shuffleFromArray(sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_FLOAT_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(float[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Float512Vector.class, float.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_FLOAT_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(float[] a, int ax, Mask<Float, Shapes.S512Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Float512Vector oldVal = SPECIES.fromArray(a, ax);
        Float512Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        // @@@ Endianess
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Float512Vector.class, float.class, LENGTH,
                               a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                   FloatBuffer tb = bbc.asFloatBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int ix, Mask<Float, Shapes.S512Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Float512Vector oldVal = SPECIES.fromByteArray(a, ix);
        Float512Vector newVal = oldVal.blend(this, m);
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
        VectorIntrinsics.store(Float512Vector.class, float.class, LENGTH,
                               U.getObject(bb, BYTE_BUFFER_HB), ix + U.getLong(bb, BUFFER_ADDRESS),
                               this,
                               bb, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                   FloatBuffer tb = bbc.asFloatBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Float, Shapes.S512Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Float512Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Float512Vector newVal = oldVal.blend(this, m);
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
        Float512Vector that = (Float512Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Float512Mask bTest(Vector<Float, Shapes.S512Bit> o, FBinTest f) {
        float[] vec1 = getElements();
        float[] vec2 = ((Float512Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Float512Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Float512Mask equal(Vector<Float, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Float512Vector.class, Float512Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Float512Mask notEqual(Vector<Float, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Float512Vector.class, Float512Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Float512Mask lessThan(Vector<Float, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Float512Vector.class, Float512Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Float512Mask lessThanEq(Vector<Float, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Float512Vector.class, Float512Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Float512Mask greaterThan(Vector<Float, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;

        return (Float512Mask) VectorIntrinsics.compare(
            BT_gt, Float512Vector.class, Float512Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Float512Mask greaterThanEq(Vector<Float, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Float512Vector v = (Float512Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Float512Vector.class, Float512Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        float[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Float, Shapes.S512Bit> o, FUnCon f) {
        boolean[] mbits = ((Float512Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }

    Int512Vector toBits() {
        float[] vec = getElements();
        int[] res = new int[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.floatToIntBits(vec[i]);
        }
        return new Int512Vector(res);
    }


    @Override
    public Float512Vector rotateEL(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Float512Vector(res);
    }

    @Override
    public Float512Vector rotateER(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Float512Vector(res);
    }

    @Override
    public Float512Vector shiftEL(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Float512Vector(res);
    }

    @Override
    public Float512Vector shiftER(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Float512Vector(res);
    }

    @Override
    @ForceInline
    public Float512Vector rearrange(Vector<Float, Shapes.S512Bit> v,
                                  Shuffle<Float, Shapes.S512Bit> s, Mask<Float, Shapes.S512Bit> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Float512Vector rearrange(Shuffle<Float, Shapes.S512Bit> o1) {
    Objects.requireNonNull(o1);
    Float512Shuffle s =  (Float512Shuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            Float512Vector.class, Float512Shuffle.class, float.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
            float[] vec = this.getElements();
            int ei = s_.getElement(i);
            return vec[ei];
        }));
    }

    @Override
    @ForceInline
    public Float512Vector blend(Vector<Float, Shapes.S512Bit> o1, Mask<Float, Shapes.S512Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Float512Vector v = (Float512Vector)o1;
        Float512Mask   m = (Float512Mask)o2;

        return VectorIntrinsics.blend(
            Float512Vector.class, Float512Mask.class, float.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public float get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        int bits = (int) VectorIntrinsics.extract(
                                Float512Vector.class, float.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    float[] vecarr = vec.getElements();
                                    return (long)Float.floatToIntBits(vecarr[ix]);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    public Float512Vector with(int i, float e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Float512Vector.class, float.class, LENGTH,
                                this, i, (long)Float.floatToIntBits(e),
                                (v, ix, bits) -> {
                                    float[] res = v.getElements().clone();
                                    res[ix] = Float.intBitsToFloat((int)bits);
                                    return new Float512Vector(res);
                                });
    }

    // Mask

    static final class Float512Mask extends AbstractMask<Float, Shapes.S512Bit> {
        static final Float512Mask TRUE_MASK = new Float512Mask(true);
        static final Float512Mask FALSE_MASK = new Float512Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Float512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Float512Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Float512Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Float512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Float512Mask(res);
        }

        @Override
        Float512Mask bOp(Mask<Float, Shapes.S512Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Float512Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Float512Mask(res);
        }

        @Override
        public Float512Species species() {
            return SPECIES;
        }

        @Override
        public Float512Vector toVector() {
            float[] res = new float[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (float) (bits[i] ? -1 : 0);
            }
            return new Float512Vector(res);
        }

        // Unary operations

        @Override
        @ForceInline
        public Float512Mask not() {
            return (Float512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Float512Mask.class, int.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Float512Mask and(Mask<Float,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Float512Mask m = (Float512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Float512Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Float512Mask or(Mask<Float,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Float512Mask m = (Float512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Float512Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Float512Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Float512Mask.class, int.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Float512Shuffle extends AbstractShuffle<Float, Shapes.S512Bit> {
        Float512Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Float512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Float512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Float512Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public Float512Species species() {
            return SPECIES;
        }

        @Override
        public Float512Vector toVector() {
            float[] va = new float[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (float) getElement(i);
            }
            return species().fromArray(va, 0);
        }

        @Override
        public Float512Shuffle rearrange(Vector.Shuffle<Float, Shapes.S512Bit> o) {
            Float512Shuffle s = (Float512Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Float512Shuffle(r);
        }
    }

    // Species

    @Override
    public Float512Species species() {
        return SPECIES;
    }

    static final class Float512Species extends FloatSpecies<Shapes.S512Bit> {
        static final int BIT_SIZE = Shapes.S_512_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Float.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(float.class.getSimpleName()).append("s x ");
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
        public Class<Float> elementType() {
            return float.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Float.SIZE;
        }

        @Override
        @ForceInline
        public Shapes.S512Bit shape() {
            return Shapes.S_512_BIT;
        }

        @Override
        Float512Vector op(FOp f) {
            float[] res = new float[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Float512Vector(res);
        }

        @Override
        Float512Vector op(Mask<Float, Shapes.S512Bit> o, FOp f) {
            float[] res = new float[length()];
            boolean[] mbits = ((Float512Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Float512Vector(res);
        }

        // Factories

        @Override
        public Float512Mask maskFromValues(boolean... bits) {
            return new Float512Mask(bits);
        }

        @Override
        public Float512Mask maskFromArray(boolean[] bits, int i) {
            return new Float512Mask(bits, i);
        }

        @Override
        public Float512Shuffle shuffle(IntUnaryOperator f) {
            return new Float512Shuffle(f);
        }

        @Override
        public Float512Shuffle shuffleIota() {
            return new Float512Shuffle(AbstractShuffle.IDENTITY);
        }

        @Override
        public Float512Shuffle shuffleFromValues(int... ixs) {
            return new Float512Shuffle(ixs);
        }

        @Override
        public Float512Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Float512Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Float512Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Float512Vector.class, float.class, LENGTH,
                                                     Float.floatToIntBits(0.0f),
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Float512Vector broadcast(float e) {
            return VectorIntrinsics.broadcastCoerced(
                Float512Vector.class, float.class, LENGTH,
                Float.floatToIntBits(e),
                ((long bits) -> SPECIES.op(i -> Float.intBitsToFloat((int)bits))));
        }

        @Override
        @ForceInline
        public Float512Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Float512Mask.class, int.class, LENGTH,
                                                     (int)-1,
                                                     (z -> Float512Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Float512Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Float512Mask.class, int.class, LENGTH,
                                                     0,
                                                     (z -> Float512Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Float512Vector scalars(float... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return VectorIntrinsics.load(Float512Vector.class, float.class, LENGTH,
                                         es, Unsafe.ARRAY_FLOAT_BASE_OFFSET,
                                         es, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Float512Vector fromArray(float[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return VectorIntrinsics.load(Float512Vector.class, float.class, LENGTH,
                                         a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_FLOAT_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Float512Vector fromArray(float[] a, int ax, Mask<Float, Shapes.S512Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromArray(a, ax), m);
        }

        @Override
        @ForceInline
        public Float512Vector fromByteArray(byte[] a, int ix) {
            // @@@ Endianess
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Float512Vector.class, float.class, LENGTH,
                                         a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                             FloatBuffer tb = bbc.asFloatBuffer();
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Float512Vector fromByteArray(byte[] a, int ix, Mask<Float, Shapes.S512Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Float512Vector fromByteBuffer(ByteBuffer bb, int ix) {
            // @@@ Endianess
            if (bb.order() != ByteOrder.nativeOrder()) {
                throw new IllegalArgumentException();
            }
            ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Float512Vector.class, float.class, LENGTH,
                                         U.getObject(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                         bb, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                             FloatBuffer tb = bbc.asFloatBuffer();
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Float512Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Float, Shapes.S512Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> Float512Vector cast(Vector<F, T> o) {
            if (o.length() != LENGTH)
                throw new IllegalArgumentException("Vector length this species length differ");

            return VectorIntrinsics.cast(
                o.getClass(),
                o.elementType(), LENGTH,
                float.class, LENGTH,
                o, this,
                (s, v) -> s.castDefault(v)
            );
        }

        @SuppressWarnings("unchecked")
        @ForceInline
        private <F, T extends Shape> Float512Vector castDefault(Vector<F, T> v) {
            // Allocate array of required size
            int limit = length();
            float[] a = new float[limit];

            Class<?> vtype = v.species().elementType();
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Float512Mask cast(Mask<E, S> m) {
            if (m.length() != LENGTH)
                throw new IllegalArgumentException("Mask length this species length differ");
            return new Float512Mask(m.toArray());
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Float512Shuffle cast(Shuffle<E, S> s) {
            if (s.length() != LENGTH)
                throw new IllegalArgumentException("Shuffle length this species length differ");
            return new Float512Shuffle(s.toArray());
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> Float512Vector rebracket(Vector<F, Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte512Vector.class,
                    byte.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short512Vector.class,
                    short.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int512Vector.class,
                    int.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class,
                    long.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float512Vector.class,
                    float.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double512Vector.class,
                    double.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Float512Vector resize(Vector<Float, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float64Vector.class,
                    float.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float128Vector.class,
                    float.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float256Vector.class,
                    float.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float512Vector.class,
                    float.class, so.length(),
                    float.class, LENGTH,
                    so, this,
                    (s, v) -> (Float512Vector) s.reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}