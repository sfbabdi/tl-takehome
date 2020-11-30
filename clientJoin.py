#!/usr/bin/env python3

import pandas as pd
import code

browser = pd.read_csv('data/browser.csv')
os = pd.read_csv('data/os.csv')
domain_dtype = {'domain_name': str, 'tl_domain_id': 'Int64'}
domain = pd.read_csv('data/domain.csv', dtype=domain_dtype)

client_log = pd.read_csv('data/clientLog.csv')

tl_dtype = {
    'auction_id': 'uint64',
    'timestamp': str,
    'exchange_supplier_id': 'Int64',
    'an_tag_id,user_id': 'Int64',
    'reserve_price': 'Int64',
    'ip_addr': str,
    'country': str,
    'region': str,
    'city': str,
    'postal_code': str,
    'dma': 'Int64',
    'tl_domain_id': 'Int64',
    'device_type': 'Int64',
    'tl_placement_id': 'Int64',
    'tl_publisher_id': 'Int64',
    'tl_seller_member_id': 'Int64',
    'tl_browser_id': 'Int64',
    'tl_os_id': 'Int64',
    'age': 'Int64',
    'datacenter_region': int,
    'url': str,
}
tl_parse_dates = ['timestamp']
tl_log = pd.read_csv('data/tlLog.csv', dtype=tl_dtype, parse_dates=tl_parse_dates)

client_log = client_log.merge(browser, how='left', on='browser_name')
client_log = client_log.merge(os, how='left', on='os_name')
client_log = client_log.merge(domain, how='left', on='domain_name')

#code.interact(local=locals())

result = tl_log.merge(client_log, how='outer', on=['tl_browser_id', 'tl_domain_id', 'tl_os_id'])

