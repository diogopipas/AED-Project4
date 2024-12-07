package aed.matrix;

import aed.tables.OpenAddressingHashTable;

public class Sparse2DMatrix {

    private final int lines;
    private final int columns;
    private final OpenAddressingHashTable<String, Float> matrix;

    public Sparse2DMatrix(int lines, int columns) {
        if (lines <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive.");
        }
        this.lines = lines;
        this.columns = columns;
        this.matrix = new OpenAddressingHashTable<>();
    }

    private String encodeKey(int line, int column) {
        return line + "," + column;
    }

    private int[] decodeKey(String key) {
        String[] parts = key.split(",");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    public int getNumberNonZero() {
        return matrix.size();
    }

    public void put(int line, int column, float value) {
        if (line < 0 || line >= lines || column < 0 || column >= columns) {
            throw new IndexOutOfBoundsException("Invalid matrix position.");
        }

        String key = encodeKey(line, column);
        if (value == 0) {
            matrix.delete(key);
        } else {
            matrix.put(key, value);
        }
    }

    public float get(int line, int column) {
        if (line < 0 || line >= lines || column < 0 || column >= columns) {
            throw new IndexOutOfBoundsException("Invalid matrix position.");
        }

        String key = encodeKey(line, column);
        Float value = matrix.get(key);
        return value == null ? 0 : value;
    }

    public Sparse2DMatrix scalar(float scalar) {
        Sparse2DMatrix result = new Sparse2DMatrix(this.lines, this.columns);

        for (String key : this.matrix.keys()) {
            float value = this.matrix.get(key);
            float scaledValue = value * scalar;

            if (scaledValue != 0) {
                result.matrix.put(key, scaledValue);
            }
        }

        return result;
    }


    public Sparse2DMatrix sum(Sparse2DMatrix b) {
        if (this.lines != b.lines || this.columns != b.columns) {
            throw new IllegalArgumentException("Matrices dimensions must match for addition.");
        }

        Sparse2DMatrix result = new Sparse2DMatrix(lines, columns);

        for (String key : this.matrix.keys()) {
            result.matrix.put(key, this.matrix.get(key));
        }

        for (String key : b.matrix.keys()) {
            Float valueA = result.matrix.get(key);
            Float valueB = b.matrix.get(key);
            float sum = (valueA == null ? 0 : valueA) + valueB;
            if (sum != 0) {
                result.matrix.put(key, sum);
            } else {
                result.matrix.delete(key);
            }
        }

        return result;
    }


    public Sparse2DMatrix multiply(Sparse2DMatrix b) {
        if (this.columns != b.lines) {
            throw new IllegalArgumentException("Matrices dimensions must be compatible for multiplication.");
        }

        Sparse2DMatrix result = new Sparse2DMatrix(this.lines, b.columns);

        OpenAddressingHashTable<Integer, OpenAddressingHashTable<Integer, Float>> bColumnMap = new OpenAddressingHashTable<>();

        for (String keyB : b.matrix.keys()) {
            int[] posB = b.decodeKey(keyB);
            int rowB = posB[0];
            int colB = posB[1];

            if (bColumnMap.get(rowB) == null) {
                bColumnMap.put(rowB, new OpenAddressingHashTable<>());
            }

            bColumnMap.get(rowB).put(colB, b.matrix.get(keyB));
        }

        for (String keyA : this.matrix.keys()) {
            int[] posA = this.decodeKey(keyA);
            int rowA = posA[0];
            int colA = posA[1];
            float valueA = this.matrix.get(keyA);

            OpenAddressingHashTable<Integer, Float> bRow = bColumnMap.get(colA);
            if (bRow != null) {
                for (Integer colB : bRow.keys()) {
                    String resultKey = encodeKey(rowA, colB);
                    float existingValue = result.matrix.get(resultKey) == null ? 0 : result.matrix.get(resultKey);
                    float newValue = existingValue + valueA * bRow.get(colB);

                    if (newValue != 0) {
                        result.matrix.put(resultKey, newValue);
                    } else {
                        result.matrix.delete(resultKey);
                    }
                }
            }
        }

        return result;
    }


    public float[] getNonZeroElements() {
        float[] elements = new float[matrix.size()];
        int index = 0;

        for (String key : matrix.keys()) {
            elements[index++] = matrix.get(key);
        }

        return elements;
    }

    public float[][] getNonSparseMatrix() {
        float[][] fullMatrix = new float[lines][columns];

        for (String key : matrix.keys()) {
            int[] pos = decodeKey(key);
            fullMatrix[pos[0]][pos[1]] = matrix.get(key);
        }

        return fullMatrix;
    }

    public static void main(String[] args) {
        Sparse2DMatrix matrixA = new Sparse2DMatrix(3, 3);
        matrixA.put(0, 0, 1);
        matrixA.put(1, 1, 2);
        matrixA.put(2, 2, 3);

        System.out.println("Number of non-zero elements: " + matrixA.getNumberNonZero());
        System.out.println("Element at (1, 1): " + matrixA.get(1, 1));
        System.out.println("Element at (0, 2): " + matrixA.get(0, 2));

        Sparse2DMatrix scaledMatrix = matrixA.scalar(2);
        System.out.println("Scaled matrix non-zero elements: " + scaledMatrix.getNumberNonZero());

        Sparse2DMatrix matrixB = new Sparse2DMatrix(3, 3);
        matrixB.put(0, 0, 4);
        matrixB.put(1, 1, 5);
        matrixB.put(2, 2, 6);

        Sparse2DMatrix sumMatrix = matrixA.sum(matrixB);
        System.out.println("Sum matrix non-zero elements: " + sumMatrix.getNumberNonZero());

        Sparse2DMatrix productMatrix = matrixA.multiply(matrixB);
        System.out.println("Product matrix non-zero elements: " + productMatrix.getNumberNonZero());

        Sparse2DMatrix hugeMatrix = new Sparse2DMatrix(1000000, 1000000);


        for (int i = 0; i < 10000; i++) {
            int randomRow = (int) (Math.random() * 1000000);
            int randomCol = (int) (Math.random() * 1000000);
            float randomValue = (float) (Math.random() * 10);
            hugeMatrix.put(randomRow, randomCol, randomValue);
        }

        Sparse2DMatrix hugeScaledMatrix = hugeMatrix.scalar(2.0f);

        System.out.println("Number of non-zero elements: " + hugeScaledMatrix.getNumberNonZero());

    }
}
