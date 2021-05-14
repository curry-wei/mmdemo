#!/usr/bin/env python

import argparse
import logging
import os
import sys


def parse_args():
    parser = argparse.ArgumentParser(description='mmsdk aar generator.')
    parser.add_argument('--make', action='store_true', help='make aar file of sdk')
    parser.add_argument('--clean', action='store_true', default=False, help='clean sdk build and output dir')
    parser.add_argument('--install', action='store_true', default=False, help='copy aar to mmdemo libs')
    return parser.parse_args()


def main():
    logging.basicConfig(level=logging.DEBUG)
    args = parse_args()
    if args.clean:
        logging.info(' -> clean sdk build and output dir')
        os.system('./gradlew :mmsdk:clean cleanSdk')
        return

    if args.install:
        logging.info(' -> copy aar to mmdemo/libs')
        os.system('./gradlew installSdk')
        return

    if args.make:
        logging.info(' -> make aar file of sdk')
        os.system('./gradlew cleanSdk :mmsdk:zipAar')
        return


if __name__ == '__main__':
    sys.exit(main())
