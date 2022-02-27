from multiprocessing import Process, Value, Array, Manager
from numpy.random import choice
import sys, random, time, json, os


class Solver:
  def __init__(self, filename):
      pass

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
