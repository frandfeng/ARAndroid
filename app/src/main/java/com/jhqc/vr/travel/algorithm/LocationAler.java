package com.jhqc.vr.travel.algorithm;

import com.jhqc.vr.travel.util.LogUtils;

public class LocationAler {

	/** 空间已知4点坐标 */
	float p[][] = new float[4][3];
	/** 空间已知4点距离*/
	float d[] = new float[4];

 	/** 初始化空间4点坐标
	  坐标,数组
	  num:1-4 */
	public void set_point(float point[], int num)
	{
		int j = 0;

		for (j = 0;j < 3;j++)
		{
			p[num][j] = point[j];
		}
	}

 	/** 初始化空间4点距离
	  distance:距离
	  num:0-3 */
	public void set_distance(float distance, int num)
	{
		d[num] = distance;
	}

 	/** 计算未知点坐标
	  p:计算后的返回值
	  fail:back -1 */
	public float[] calc() throws Exception
	{
		float point[];
		//矩阵A
		float A[][] = new float[3][3];
		//矩阵B
		float B[]= new float[3];
		int i = 0;
		int j = 0;

		//初始化B矩阵
		for (i = 0;i < 3;i++)
		{
			B[i] = (LocationMath.d_p_square(p[i + 1]) - LocationMath.d_p_square(p[i]) - (d[i + 1] * d[i + 1] - d[i] * d[i])) / 2;
		}

		//初始化A矩阵
		for (i = 0;i < 3;i++) {
			for (j = 0;j < 3;j++)
			{
				A[i][j] = p[i + 1][j] - p[i][j];
			}
		}

		//计算未知点坐标
		point = LocationMath.solve(A, B);

		return point;
	}

	public static void main(String args[]) {

		try{

			float point[]=new float[3];
			LocationAler loc = new LocationAler();

			//获得坐标
			point[0] = 0;
			point[1] = 0;
			point[2] = (float) 0.5;
			loc.set_point(point,1);

			point[0] = 0;
			point[1] = -1;
			point[2] = 2;
			loc.set_point(point,2);

			point[0] = 0;
			point[1] = 1;
			point[2] = 0;
			loc.set_point(point,3);

			point[0] = 1;
			point[1] = 0;
			point[2] = 3;
			loc.set_point(point,4);

			//distance
			loc.set_distance(1,1);
			loc.set_distance(1,2);
			loc.set_distance(2,3);
			loc.set_distance(1,4);

			//calc
			float x[] = loc.calc();
			if (x == null)
			{
				System.out.println("fail");
			}
			else
			{
				System.out.println(x[0]+","+x[1]+","+ x[2]);
			}

		} catch(Exception ex){
			ex.printStackTrace();
		}

	}

}
