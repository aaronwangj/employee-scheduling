from multiprocessing import Process, Value, Array, Manager
from numpy.random import choice
import numpy as np
import sys, random, time, json, os
from collections import defaultdict

# CP model
from docplex.cp.model import CpoModel

# LP/IP models
from docplex.mp.model import Model


class Solver:
    def __init__(self, filename):
        # Parse .sched file.
        print('FILENAME:', filename)
        with open(filename, 'r') as file:
            raw_file = file.read().split('\n')
        parsed_file = []
        for line in raw_file:
            colon_index = line.index(':')
            parsed_file.append(line[colon_index + 1:].rstrip().lstrip())

        # Create and assign variables from .sched file.
        self.filename = filename
        self.numWeeksB = int(parsed_file[0])
        self.numDaysB = int(parsed_file[1])
        self.numEmployeesB = int(parsed_file[2])
        self.numShiftsB = int(parsed_file[3])
        self.numIntervalsInDayB = int(parsed_file[4])
        self.minDailyOperationB = int(parsed_file[6])
        self.minConsecutiveWorkE = int(parsed_file[7])
        self.maxDailyWorkE = int(parsed_file[8])
        self.minWeeklyWorkE = int(parsed_file[9])
        self.maxWeeklyWorkE = int(parsed_file[10])
        self.nmaxConsecutiveNigthShiftE = int(parsed_file[11])
        self.maxTotalNigthShiftE = int(parsed_file[12])
        self.minDemandDayShiftB = np.array([int(x) for x in parsed_file[5].split(' ')]).reshape(self.numDaysB,
                                                                                                self.numShiftsB)
        self.cp = CpoModel()
        self.results = defaultdict(str)

    def solve(self):
        self.cp.solve(execfile='/Applications/CPLEX_Studio201/cpoptimizer/bin/x86-64_osx/cpoptimizer')

        #each element in sched is (shift number, hours worked for that shift)
        #dim: num employees x num days
        sched = [[0 for _ in range(self.numDaysB)] for _ in range(self.numEmployeesB)]

        for emp in range(self.numEmployeesB):
            for day in range(self.numDaysB):
                # define variables for schedule
                sched[emp][day] = self.cp.integer_var(0, self.numShiftsB), self.cp.integer_var(0, self.maxDailyWorkE + 1)

        print('DOWN LENGTH: ', len(sched))
        print('ACROSS WIDTH: ', len(sched[0]))
        # there is a minimum demand that needs to be met to ensure the daily operation (minDailyOperation) for every day when considering all employees and shifts.
        for day in range(self.numDaysB):
            tuples_for_day = [x[day] for x in sched]
            shifts_for_day = [x[0] for x in tuples_for_day]
            # print(shifts_for_day)
            # first_element = shifts_for_day[0][0]
            # print(first_element)
            # self.cp.add(8 * self.numEmployeesB - 8 * shifts_for_day.count(0) >= self.minDailyOperationB)

        # post-lecture...start

        # create 2 matrices for tracking shifts and number of hours worked per employee
        shifts_sched = [[0 for _ in range(self.numDaysB)] for _ in range(self.numEmployeesB)]
        hours_sched = [[0 for _ in range(self.numDaysB)] for _ in range(self.numEmployeesB)]
        # 1 matrix for shifts
        for emp in range(self.numEmployeesB):
            for day in range(self.numDaysB):
                # define variables for schedule
                shifts_sched[emp][day] = self.cp.integer_var(0, self.numShiftsB)
        # 1 matrix for number of hours
        for emp in range(self.numEmployeesB):
            for day in range(self.numDaysB):
                # define variables for schedule
                hours_sched[emp][day] = self.cp.integer_var(0, self.maxDailyWorkE + 1)

        # TODO: link these matrices using constraints
        # print("Poo", json.dumps(shifts_sched[emp][day]))
        if shifts_sched[emp][day].equals(0):
            # off-shift
            hours_sched[emp][day] = 0
        else:
            # night, day, evening shifts
            print(type(hours_sched[emp][day].get_domain()))
            assert(hours_sched[emp][day].lb() > 3 and hours_sched[emp][day].ub() < 9)

        # post-lecture...end





        # print(sched)
        self.results['Instance'] = self.filename
        self.results['Time'] = '1.23'
        self.results['Result'] = '123'
        self.results['Solution'] = '8 16 8 16 8 12 -1 -1'
        return self.results

def main():
    args = sys.argv[1]
    # if len(args) != 2:
    #   print('usage error : python3 employee_scheduling.py [FILENAME.sched]')
    #   return
    solver = Solver(args)
    res = solver.solve()
    print(json.dumps(res))


if __name__ == '__main__':
    main()
