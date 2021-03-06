package is.image.threshold;

import is.image.BilevelImage;
import is.image.GrayscaleImage;
import is.image.util.Histogram;

public class OtsuThresholder
    implements Thresholder
{
    public int[] smooth(int[] arr, int neighborhood)
    {
        int n = (neighborhood-1)/2; //n must be odd
        int[] out = new int[arr.length];
        for(int i=0; i<arr.length; ++i)
        {
            int val = 0;
            for(int j=-n; j<=n; ++j)
            {
                if(i+j >= 0 && i+j < arr.length)
                {
                    val += arr[i+j];
                }
            }
            out[i] = val/neighborhood;
        }
        return out;
    }

    public BilevelImage threshold(GrayscaleImage input)
    {
        System.out.println("Performing Otsu Thresholding...");
        int rows = input.getRows();
        int cols = input.getCols();
        int maxval = input.getMaxval();

        //compute histogram
        int[] histogram = new int[maxval+1];
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int val = input.get(row, col);
                if(val > maxval)
                {
                    System.out.println("Bad value of "+val+" at ("+row+", "+col+")");
                }
                else
                {
                    histogram[val]++;
                }
            }
        }
        histogram = smooth(histogram, 3);

        //Histogram.writeHistogramImage("threshold.pbm", histogram);

        //convert to probabilities
        int total = rows*cols;
        double[] prob = new double[maxval];
        for(int i=0; i<prob.length; ++i)
        {
            prob[i] = ((double)histogram[i])/total;
        }

        //compute mu
        double mu = 0.0;
        for(int i=1; i<=prob.length; ++i)
        {
            mu += i * prob[i-1];
        }

        double q1 = 0.0;
        double q2 = 0.0;
        double mu1 = 0.0;
        double mu2 = 0.0;
        double max = 0.0;
        int max_t = 0;

        for(int i=1; i<=prob.length; ++i)
        {
            mu1 = q1*mu1 + i*prob[i-1];
            q1 += prob[i-1];
            q2 = 1.0 - q1;
            if(q1 != 0.0) mu1 = mu1/q1;
            mu2 = (mu - q1*mu1)/q2;
            double diff = mu1 - mu2;
            double curr = q1*q2*diff*diff;
            //System.out.println(curr);
            if(curr > max)
            {
                max = curr;
                max_t = i-1;
            }
        }

        double threshold = ((double)max_t)/maxval;
        System.out.println("Threshold: "+threshold);
        return (new GlobalThresholder(threshold)).threshold(input);
    }
}
