package solver.cp;

import ilog.cp.*;

import ilog.concert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import java.util.Scanner;

public class CPInstance
{
  // BUSINESS parameters
  int numWeeks;
  int numDays;
  int numEmployees;
  int numShifts;
  int numIntervalsInDay;
  int[][] minDemandDayShift;
  int minDailyOperation;

  // EMPLOYEE parameters
  int minConsecutiveWork;
  int maxDailyWork;
  int minWeeklyWork;
  int maxWeeklyWork;
  int maxConsecutiveNightShift;
  int maxTotalNightShift;
  StringBuilder stb;
  // ILOG CP Solver
  IloCP cp;

  public CPInstance(String fileName)
  {
    try
    {
      Scanner read = new Scanner(new File(fileName));
      this.stb = new StringBuilder();

      while (read.hasNextLine())
      {
        String line = read.nextLine();
        String[] values = line.split(" ");
        if(values[0].equals("Business_numWeeks:"))
        {
          numWeeks = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_numDays:"))
        {
          numDays = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_numEmployees:"))
        {
          numEmployees = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_numShifts:"))
        {
          numShifts = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_numIntervalsInDay:"))
        {
          numIntervalsInDay = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_minDemandDayShift:"))
        {
          int index = 1;
          minDemandDayShift = new int[numDays][numShifts];
          for(int d=0; d<numDays; d++)
            for(int s=0; s<numShifts; s++)
              minDemandDayShift[d][s] = Integer.parseInt(values[index++]);
        }
        else if(values[0].equals("Business_minDailyOperation:"))
        {
          minDailyOperation = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_minConsecutiveWork:"))
        {
          minConsecutiveWork = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_maxDailyWork:"))
        {
          maxDailyWork = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_minWeeklyWork:"))
        {
          minWeeklyWork = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_maxWeeklyWork:"))
        {
          maxWeeklyWork = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_maxConsecutiveNigthShift:"))
        {
          maxConsecutiveNightShift = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_maxTotalNigthShift:"))
        {
          maxTotalNightShift = Integer.parseInt(values[1]);
        }
      }
    }
    catch (FileNotFoundException e)
    {
      System.out.println("Error: file not found " + fileName);
    }
  }

  public void solve()
  {
    try
    {
      cp = new IloCP();
      IloIntExpr zero = cp.intVar(0, 0);
      IloIntExpr one = cp.intVar(1, 1);
      IloIntExpr two = cp.intVar(2, 2);
      IloIntExpr three = cp.intVar(3, 3);
      IloIntExpr four = cp.intVar(4, 4);
      IloIntExpr twenty = cp.intVar(20, 20);
      IloIntExpr forty = cp.intVar(40, 40);
      IloIntExpr mdo = cp.intVar(this.minDailyOperation, this.minDailyOperation);


      // TODO: Employee Scheduling Model Goes Here

      // Important: Do not change! Keep these parameters as is
      cp.setParameter(IloCP.IntParam.Workers, 1);
      cp.setParameter(IloCP.DoubleParam.TimeLimit, 300);
      // cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);

      // Uncomment this: to set the solver output level if you wish
      cp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Quiet);

      /*
      shiftSchedule: num employees x numdays (arraylist[IloNumVar[]])
                     each element tells us which shift employee_i works on day d
      durationSchedule: num employees x numdays (arraylist[IloNumVar[]])
                        each element tells us how many hours employee_i works on day d
      NOTE: each element is of type IloNumVar
       */
      ArrayList<IloIntExpr[]> shiftSchedule = new ArrayList<IloIntExpr[]>();
      ArrayList<IloIntExpr[]> durationSchedule = new ArrayList<IloIntExpr[]>();

      /*
      filling matrices with variables
      */
      for (int i = 0; i < this.numEmployees; i++) {
        shiftSchedule.add(new IloIntExpr[this.numDays]);
        IloIntExpr[] x = shiftSchedule.get(i);
        for (int j = 0; j < this.numDays; j++) {
          x[j] = cp.intVar(0, 3);
        }

        durationSchedule.add(new IloIntExpr[this.numDays]);
        IloIntExpr[] y = durationSchedule.get(i);
        for (int j = 0; j < this.numDays; j++) {
          y[j] = cp.intVar(0, 8);
        }
      }



      /*
      minDemandDayShift
      */
      for (int i = 0; i < this.numDays; i++) {
        for (int j = 0; j < this.numShifts; j++) {
          int minDemand = minDemandDayShift[i][j];
          IloIntExpr[] allShifts = new IloIntExpr[this.numEmployees];
          for (int k = 0; k < this.numEmployees; k++) {
            IloIntExpr[] empSched = shiftSchedule.get(k);
            allShifts[k] = empSched[i];
          }
          cp.add(cp.ge(zero, cp.diff(minDemand, cp.count(allShifts, j))));

        }
      }


      /*
      minDailyOperation
      */
      for (int i = 0; i < this.numDays; i ++) {

        IloIntExpr[] allDurations = new IloIntExpr[this.numEmployees];
        for (int k = 0; k < this.numEmployees; k++) {
          IloIntExpr[] empSched = durationSchedule.get(k);
          allDurations[k] = empSched[i];
        }

        cp.add(cp.ge(cp.sum(allDurations), mdo));

      }


      for (int i = 0; i < this.numEmployees; i ++) {
        IloIntExpr[] allShifts = new IloIntExpr[this.numDays];
        /*
        first four days
        */
        if (this.numDays >= 4) {
          IloIntExpr[] shifts = new IloIntExpr[4];
          shifts[0] = shiftSchedule.get(i)[0];
          shifts[1] = shiftSchedule.get(i)[1];
          shifts[2] = shiftSchedule.get(i)[2];
          shifts[3] = shiftSchedule.get(i)[3];
          cp.add(cp.allDiff(shifts));
        } else {
          IloIntExpr[] shifts = new IloIntExpr[this.numDays];

          for (int j = 0; j < this.numDays; j++) {
            shifts[j] = shiftSchedule.get(i)[j];
          }
          cp.add(cp.allDiff(shifts));
        }
        for (int j = 0; j < this.numDays; j ++) {

          /*
            if duration is not zero, shift is not zero
            if shift is not zero, duration is not zero
          */
          cp.add(cp.ifThen(cp.neq(durationSchedule.get(i)[j], zero), cp.neq(shiftSchedule.get(i)[j], zero)));
          cp.add(cp.ifThen(cp.neq(shiftSchedule.get(i)[j], zero), cp.neq(durationSchedule.get(i)[j], zero)));

          /*
          duration >= 4, <=8
          */
          cp.add(cp.ifThen(cp.neq(shiftSchedule.get(i)[j], zero), cp.ge(durationSchedule.get(i)[j], four)));


          /*
          min/max weekly duration
          */
          if ((j == 0) || (j%7 == 0)){
          IloIntExpr[] allHours = new IloIntExpr[7];
          allHours[0] = durationSchedule.get(i)[j];
          allHours[1] = durationSchedule.get(i)[j+1];
          allHours[2] = durationSchedule.get(i)[j+2];
          allHours[3] = durationSchedule.get(i)[j+3];
          allHours[4] = durationSchedule.get(i)[j+4];
          allHours[5] = durationSchedule.get(i)[j+5];
          allHours[6] = durationSchedule.get(i)[j+6];

          cp.add(cp.ge(cp.sum(allHours), twenty));
          cp.add(cp.le(cp.sum(allHours), forty));
        }
        allShifts[j] = shiftSchedule.get(i)[j];
        if (j > 0){
          cp.add(cp.ifThen(cp.eq(allShifts[j], one), cp.neq(allShifts[j-1], one)));
        }
        }
        cp.add(cp.ge(this.maxTotalNightShift, cp.count(allShifts, 1)));
      }

      if(cp.solve()) {
        // cp.printInformation();

        // Uncomment this: for poor man's Gantt Chart to display schedules

        // int[][] beginED = new int[this.numEmployees][this.numDays];
        // int[][] endED = new int[this.numEmployees][this.numDays];
        //
        // for (int i = 0; i < this.numEmployees; i ++) {
        //   IloIntExpr[] empShifts = shiftSchedule.get(i);
        //   IloIntExpr[] empDurations = durationSchedule.get(i);
        //   for (int j = 0; j < this.numDays; j ++) {
        //     if ((int)cp.getValue(empShifts[j]) == 0) {
        //       beginED[i][j] = -1;
        //       endED[i][j] = -1;
        //     } else if ((int)cp.getValue(empShifts[j]) == 1) {
        //       beginED[i][j] = 0;
        //       endED[i][j] = beginED[i][j] + (int)cp.getValue(empDurations[j]);
        //
        //     } else if ((int)cp.getValue(empShifts[j]) == 2) {
        //       beginED[i][j] = 8;
        //       endED[i][j] = beginED[i][j] + (int)cp.getValue(empDurations[j]);
        //
        //     } else if ((int)cp.getValue(empShifts[j]) == 3) {
        //       beginED[i][j] = 16;
        //       endED[i][j] = beginED[i][j] + (int)cp.getValue(empDurations[j]);
        //     } else {
        //       System.out.println("uh oh, we somehow got a shift over 3:" + (int)cp.getValue(empShifts[j]));
        //     }
        //
        //     if ((i == this.numEmployees - 1) && (j == this.numDays-1)){
        //       this.solutionString = this.solutionString + beginED[i][j] + " " + endED[i][j];
        //     } else {
        //       this.solutionString = this.solutionString + beginED[i][j] + " " + endED[i][j] + " ";
        //     }
        //   }
        // }
        // prettyPrint(numEmployees, numDays, beginED, endED);


/*
UNCOMMENT FOR AUTOGRADER (results.log cannot have solutions)
*/
        for (int i = 0; i < this.numEmployees; i ++) {
          IloIntExpr[] empShifts = shiftSchedule.get(i);
          IloIntExpr[] empDurations = durationSchedule.get(i);
          for (int j = 0; j < this.numDays; j ++) {
            if ((int)cp.getValue(empShifts[j]) == 0) {
              if ((i == this.numEmployees - 1) && (j == this.numDays-1)){
                stb.append("-1 -1");
              } else {
                stb.append("-1 -1 ");
              }
            } else if ((int)cp.getValue(empShifts[j]) == 1) {
              if ((i == this.numEmployees - 1) && (j == this.numDays-1)){
                stb.append("0 " + Integer.toString((int)cp.getValue(empDurations[j])));
              } else {
                stb.append("0 " + Integer.toString((int)cp.getValue(empDurations[j])) + " ");
              }

            } else if ((int)cp.getValue(empShifts[j]) == 2) {
              if ((i == this.numEmployees - 1) && (j == this.numDays-1)){
                stb.append("8 " + Integer.toString(8 + (int)cp.getValue(empDurations[j])));
              } else {
                stb.append("8 " + Integer.toString(8 + (int)cp.getValue(empDurations[j])) + " ");
              }
            } else if ((int)cp.getValue(empShifts[j]) == 3) {
              if ((i == this.numEmployees - 1) && (j == this.numDays-1)){
                stb.append("16 " + Integer.toString(16 + (int)cp.getValue(empDurations[j])));
              } else {
                stb.append("16 " + Integer.toString(16 + (int)cp.getValue(empDurations[j])) + " ");
              }
            } else {
              System.out.println("uh oh, we somehow got a shift over 3:" + (int)cp.getValue(empShifts[j]));
            }
          }
        }
      }
      else
      {
        System.out.println("No Solution found!");
        System.out.println("Number of fails: " + cp.getInfo(IloCP.IntInfo.NumberOfFails));
      }
    }
    catch(IloException e)
    {
      System.out.println("Error: " + e);
    }
  }

  // SK: technically speaking, the model with the global constaints
  // should result in fewer number of fails. In this case, the problem
  // is so simple that, the solver is able to re-transform the model
  // and replace inequalities with the global all different constrains.
  // Therefore, the results don't really differ
  void solveAustraliaGlobal()
  {
    String[] Colors = {"red", "green", "blue"};
    try
    {
      cp = new IloCP();
      IloIntVar WesternAustralia = cp.intVar(0, 3);
      IloIntVar NorthernTerritory = cp.intVar(0, 3);
      IloIntVar SouthAustralia = cp.intVar(0, 3);
      IloIntVar Queensland = cp.intVar(0, 3);
      IloIntVar NewSouthWales = cp.intVar(0, 3);
      IloIntVar Victoria = cp.intVar(0, 3);

      IloIntExpr[] clique1 = new IloIntExpr[3];
      clique1[0] = WesternAustralia;
      clique1[1] = NorthernTerritory;
      clique1[2] = SouthAustralia;

      IloIntExpr[] clique2 = new IloIntExpr[3];
      clique2[0] = Queensland;
      clique2[1] = NorthernTerritory;
      clique2[2] = SouthAustralia;

      IloIntExpr[] clique3 = new IloIntExpr[3];
      clique3[0] = Queensland;
      clique3[1] = NewSouthWales;
      clique3[2] = SouthAustralia;

      IloIntExpr[] clique4 = new IloIntExpr[3];
      clique4[0] = Queensland;
      clique4[1] = Victoria;
      clique4[2] = SouthAustralia;

      cp.add(cp.allDiff(clique1));
      cp.add(cp.allDiff(clique2));
      cp.add(cp.allDiff(clique3));
      cp.add(cp.allDiff(clique4));

	  cp.setParameter(IloCP.IntParam.Workers, 1);
      cp.setParameter(IloCP.DoubleParam.TimeLimit, 300);
	  cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);

      if (cp.solve())
      {
         System.out.println();
         System.out.println( "WesternAustralia:    " + Colors[(int)cp.getValue(WesternAustralia)]);
         System.out.println( "NorthernTerritory:   " + Colors[(int)cp.getValue(NorthernTerritory)]);
         System.out.println( "SouthAustralia:      " + Colors[(int)cp.getValue(SouthAustralia)]);
         System.out.println( "Queensland:          " + Colors[(int)cp.getValue(Queensland)]);
         System.out.println( "NewSouthWales:       " + Colors[(int)cp.getValue(NewSouthWales)]);
         System.out.println( "Victoria:            " + Colors[(int)cp.getValue(Victoria)]);
      }
      else
      {
        System.out.println("No Solution found!");
      }
    } catch (IloException e)
    {
      System.out.println("Error: " + e);
    }
  }

  void solveAustraliaBinary()
  {
    String[] Colors = {"red", "green", "blue"};
    try
    {
      cp = new IloCP();
      IloIntVar WesternAustralia = cp.intVar(0, 3);
      IloIntVar NorthernTerritory = cp.intVar(0, 3);
      IloIntVar SouthAustralia = cp.intVar(0, 3);
      IloIntVar Queensland = cp.intVar(0, 3);
      IloIntVar NewSouthWales = cp.intVar(0, 3);
      IloIntVar Victoria = cp.intVar(0, 3);

      cp.add(cp.neq(WesternAustralia , NorthernTerritory));
      cp.add(cp.neq(WesternAustralia , SouthAustralia));
      cp.add(cp.neq(NorthernTerritory , SouthAustralia));
      cp.add(cp.neq(NorthernTerritory , Queensland));
      cp.add(cp.neq(SouthAustralia , Queensland));
      cp.add(cp.neq(SouthAustralia , NewSouthWales));
      cp.add(cp.neq(SouthAustralia , Victoria));
      cp.add(cp.neq(Queensland , NewSouthWales));
      cp.add(cp.neq(NewSouthWales , Victoria));

	  cp.setParameter(IloCP.IntParam.Workers, 1);
      cp.setParameter(IloCP.DoubleParam.TimeLimit, 300);
	  cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);

      if (cp.solve())
      {
         System.out.println();
         System.out.println( "WesternAustralia:    " + Colors[(int)cp.getValue(WesternAustralia)]);
         System.out.println( "NorthernTerritory:   " + Colors[(int)cp.getValue(NorthernTerritory)]);
         System.out.println( "SouthAustralia:      " + Colors[(int)cp.getValue(SouthAustralia)]);
         System.out.println( "Queensland:          " + Colors[(int)cp.getValue(Queensland)]);
         System.out.println( "NewSouthWales:       " + Colors[(int)cp.getValue(NewSouthWales)]);
         System.out.println( "Victoria:            " + Colors[(int)cp.getValue(Victoria)]);
      }
      else
      {
        System.out.println("No Solution found!");
      }
    } catch (IloException e)
    {
      System.out.println("Error: " + e);
    }
  }

  void solveSendMoreMoney()
  {
    try
    {
      // CP Solver
      cp = new IloCP();

      // SEND MORE MONEY
      IloIntVar S = cp.intVar(1, 9);
      IloIntVar E = cp.intVar(0, 9);
      IloIntVar N = cp.intVar(0, 9);
      IloIntVar D = cp.intVar(0, 9);
      IloIntVar M = cp.intVar(1, 9);
      IloIntVar O = cp.intVar(0, 9);
      IloIntVar R = cp.intVar(0, 9);
      IloIntVar Y = cp.intVar(0, 9);

      IloIntVar[] vars = new IloIntVar[]{S, E, N, D, M, O, R, Y};
      cp.add(cp.allDiff(vars));

      //                1000 * S + 100 * E + 10 * N + D
      //              + 1000 * M + 100 * O + 10 * R + E
      //  = 10000 * M + 1000 * O + 100 * N + 10 * E + Y

      IloIntExpr SEND = cp.sum(cp.prod(1000, S), cp.sum(cp.prod(100, E), cp.sum(cp.prod(10, N), D)));
      IloIntExpr MORE   = cp.sum(cp.prod(1000, M), cp.sum(cp.prod(100, O), cp.sum(cp.prod(10,R), E)));
      IloIntExpr MONEY  = cp.sum(cp.prod(10000, M), cp.sum(cp.prod(1000, O), cp.sum(cp.prod(100, N), cp.sum(cp.prod(10,E), Y))));

      cp.add(cp.eq(MONEY, cp.sum(SEND, MORE)));

      // Solver parameters
      cp.setParameter(IloCP.IntParam.Workers, 1);
      cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);
      if(cp.solve())
      {
        System.out.println("  " + cp.getValue(S) + " " + cp.getValue(E) + " " + cp.getValue(N) + " " + cp.getValue(D));
        System.out.println("  " + cp.getValue(M) + " " + cp.getValue(O) + " " + cp.getValue(R) + " " + cp.getValue(E));
        System.out.println(cp.getValue(M) + " " + cp.getValue(O) + " " + cp.getValue(N) + " " + cp.getValue(E) + " " + cp.getValue(Y));
      }
      else
      {
        System.out.println("No Solution!");
      }
    } catch (IloException e)
    {
      System.out.println("Error: " + e);
    }
  }

 /**
   * Poor man's Gantt chart.
   * author: skadiogl
   *
   * Displays the employee schedules on the command line.
   * Each row corresponds to a single employee.
   * A "+" refers to a working hour and "." means no work
   * The shifts are separated with a "|"
   * The days are separated with "||"
   *
   * This might help you analyze your solutions.
   *
   * @param numEmployees the number of employees
   * @param numDays the number of days
   * @param beginED int[e][d] the hour employee e begins work on day d, -1 if not working
   * @param endED   int[e][d] the hour employee e ends work on day d, -1 if not working
   */
  void prettyPrint(int numEmployees, int numDays, int[][] beginED, int[][] endED)
  {
    for (int e = 0; e < numEmployees; e++)
    {
      System.out.print("E"+(e+1)+": ");
      if(e < 9) System.out.print(" ");
      for (int d = 0; d < numDays; d++)
      {
        for(int i=0; i < numIntervalsInDay; i++)
        {
          if(i%8==0)System.out.print("|");
          if (beginED[e][d] != endED[e][d] && i >= beginED[e][d] && i < endED[e][d]) System.out.print("+");
          else  System.out.print(".");
        }
        System.out.print("|");
      }
      System.out.println(" ");
    }
  }

}
