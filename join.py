#!/usr/bin/env python3

import pandas as pd
import code

browser = pd.read_csv('data/browser.csv')
os = pd.read_csv('data/os.csv')
domain_dtype = {'domain_name': str, 'tl_domain_id': 'Int64'}
domain = pd.read_csv('data/domain.csv', dtype=domain_dtype)

client_log = pd.read_csv('data/clientLog.csv')

client_log = client_log \
    .merge(browser, how='left', on='browser_name') \
    .merge(os, how='left', on='os_name') \
    .merge(domain, how='left', on='domain_name') \
    .sort_values(by=['tl_os_id', 'tl_browser_id', 'tl_domain_id']) \
    .reindex(columns=['tl_os_id', 'tl_browser_id', 'tl_domain_id', 'os_name', 'browser_name', 'domain_name'])
client_log.to_csv('client.csv', index=False)

#quit()

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
tl_log = pd.read_csv('data/tlLog-test.csv', dtype=tl_dtype, parse_dates=tl_parse_dates)

tl_log = tl_log \
    .sort_values(by=['tl_os_id', 'tl_browser_id', 'tl_domain_id']) \
    .merge(browser, how='left', on='tl_browser_id') \
    .merge(os, how='left', on='tl_os_id') \
    .merge(domain, how='left', on='tl_domain_id') \
    .reindex(columns=[
        'tl_os_id',
        'tl_browser_id',
        'tl_domain_id',
        'os_name',
        'browser_name',
        'domain_name',
        'auction_id',
        'timestamp',
        'exchange_supplier_id',
        'an_tag_id,user_id',
        'reserve_price',
        'ip_addr',
        'country',
        'region',
        'city',
        'postal_code',
        'dma',
        'device_type',
        'tl_placement_id',
        'tl_publisher_id',
        'tl_seller_member_id',
        'age',
        'datacenter_region',
        'url'
    ])

tl_log.to_csv('tl.csv', index=False)

#code.interact(local=locals())

