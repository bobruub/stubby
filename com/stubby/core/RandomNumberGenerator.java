package com.stubby.core;

/**
class: RandomNumberGenerator
Purpose: returns a random number for sleep time
Notes: only support UNIFORM distribution
Author: Tim Lane
Date: 24/03/2014
**/

import java.util.Random;
import java.util.Date;

public class RandomNumberGenerator {
  
    public static final String UNIFORM_RANDOM = "UNIFORM";
    public static final double waitMultplier = 1000.0;
  
    private Random _generator;
    private String _distribution;
    private double _randMin;
    private double _randMax; 
    private int _randIntMin;
    private int _randIntMax; 
  
    public RandomNumberGenerator(int randMin, int randMax){
        _generator = new Random();
        _randIntMin = randMin;
        _randIntMax = randMax;
    }
    
    public RandomNumberGenerator(String distribution, long randMin, long randMax){
        _generator = new Random();
        _distribution = distribution;
        _randMin = randMin;
        _randMax = randMax;
    }
    
    public RandomNumberGenerator(String distribution, double randMin, double randMax){
        _generator = new Random();
        _distribution = distribution;
        _randMin = randMin;
        _randMax = randMax;
    }
    
    public double randomDouble(){
        return randomDouble(_distribution, _randMin, _randMax);
    }
    
    public double randomDouble(String waitDistribution, double randMin, double randMax)
    {
        double generatorTime;
                
        if (waitDistribution.equals("UNIFORM")){
            generatorTime = uniformRandom(randMin, randMax);
        } else {
            generatorTime = 0.0;
        }
        return generatorTime;
    }
    
    public int randomDInteger(){
        return randomInteger(_randIntMin, _randIntMax);
    }
        
    public int randomInteger(int randMin, int randMax)
    {
        int generatorTime;
        generatorTime = uniformInteger(randMin, randMax);
        return generatorTime;
    }
        
    public int uniformInteger(int randMin, int randMax){
      //get the range, casting to long to avoid overflow problems
      long range = (long)randMax - (long)randMin + 1;
      // compute a fraction of the range, 0 <= frac < range
      long fraction = (long)(range * _generator.nextDouble());
      int randomNumber =  (int)(fraction + randMin);
      
     return randomNumber;
    }
    public double uniformRandom(double randMin, double randMax){
     return _generator.nextDouble() * (randMax - randMin) + randMin;
    }
    
    public double uniformLong(double randMin, double randMax){
     return _generator.nextDouble() * (randMax - randMin) + randMin;
    }
    
        
}
