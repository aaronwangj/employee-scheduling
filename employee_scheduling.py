from multiprocessing import Process, Value, Array, Manager
from numpy.random import choice
import sys, random, time, json, os

# CP model
from docplex.cp.model import CpoModel

# LP/IP models
from docplex.mp.model import Model

class Solver:
  def __init__(self, filename):
      cp = CpoModel()
      cp.solve(execfile='/Applications/CPLEX_Studio201/cpoptimizer/bin/x86-64_osx/cpoptimizer')

  def solve(self):
    print("hello!")

def main():
  args = sys.argv
  if len(args) != 2:
    print('usage error : python3 employee_scheduling.py [FILENAME.sched]')
    return
  solver = Solver(args[1])
  res = solver.solve()
  print(json.dumps(res))

if __name__ == '__main__':
  main()
