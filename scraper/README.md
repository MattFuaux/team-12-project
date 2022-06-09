# FruitWatch Fruit Price Scraper

Author: Matthew Faux

Fruitwatch Fruit Price Scraper scrapes fruit prices from Coles.

## Installation

### Prerequisite

- Python3
- Selenium (`pip3 install selenium`)
- Google Chrome
- Chrome Webdriver for your OS and chrome version (replace with existing in folder): https://chromedriver.chromium.org/downloads

### Setup

Change line 26 of `fruitscraper.py` to the chrome webdriver's path.

### Running the script

Run `fruitscraper.py` to run the scraper. The script will scrape the links defined in the `coles.csv` file and then output in JSON format (`fruitPrices,json`). The output `fruitPrices.json` is then used by the backend Go API to retrieve fruit prices for a predicted fruit.

Note: This script will be run as a cron job in our server in order to scrape the latest prices daily.
