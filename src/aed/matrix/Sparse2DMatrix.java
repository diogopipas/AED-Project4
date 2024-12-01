package aed.matrix;

import aed.tables.OpenAddressingHashTable;

public class Sparse2DMatrix {

    public Sparse2DMatrix(int lines, int columns)
    {
		//TODO: implement
    }

    public int getNumberNonZero() {
		//TODO: implement
        return 0;
    }

    public void put(int line, int column, float value)
    {
        //TODO: implement
    }

    public float get(int line, int column)
    {
        //TODO: implement
        return 0;
    }

    public Sparse2DMatrix scalar(float scalar)
    {
        //TODO: implement
        return this;
    }

    public Sparse2DMatrix sum(Sparse2DMatrix that)
    {
        //TODO: implement
        return this;
    }

    public Sparse2DMatrix multiply(Sparse2DMatrix that)
    {
		//TODO: implement
        return this;
    }
	
	public float[] getNonZeroElements()
    {
		//TODO: implement
        return new float[10];

    }

    public float[][] getNonSparseMatrix()
    {
		//TODO: implement
        return new float[10][10];
    }

    public static void main(String[] args)
    {
        //implement tests here
    }
}
