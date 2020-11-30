#!/usr/bin/env python3

"""xformLog transforms pixel firing log from TripleLift and 3rd party client log.

The log is transformed into grouped by source os,browser,domain summarized by count.
"""
import pandas as pd
import fire

xform_cols = ['tl_os_id', 'tl_browser_id', 'tl_domain_id', 'os_name', 'browser_name', 'domain_name', 'count']
sort_cols = ['tl_os_id', 'tl_browser_id', 'tl_domain_id']
reindex_cols = ['tl_os_id', 'tl_browser_id', 'tl_domain_id', 'os_name', 'browser_name', 'domain_name']

def load_support_data():
    browser = pd.read_csv('data/browser.csv')
    os = pd.read_csv('data/os.csv')
    domain_dtype = {'domain_name': str, 'tl_domain_id': 'Int64'}
    domain = pd.read_csv('data/domain.csv', dtype=domain_dtype)

    return (browser, os, domain)


def xform_client_log(input='data/clientLog.csv', output='client.csv'):
    """Transform 3rd party client log into occurance by unique os,browser,domain

    Args:
        input: 3rd party client log csv file
        output: output of transformed log
    """
    browser, os, domain = load_support_data()
    client_log = pd.read_csv(input)
    client_log_value_counts = client_log \
        .merge(browser, how='left', on='browser_name') \
        .merge(os, how='left', on='os_name') \
        .merge(domain, how='left', on='domain_name') \
        .sort_values(by=sort_cols) \
        .reindex(columns=reindex_cols) \
        .value_counts()

    client_log_xform = pd.DataFrame(client_log_value_counts) \
        .reset_index() \
        .sort_values(by=sort_cols)
    client_log_xform.columns = xform_cols
    client_log_xform.to_csv(output, index=False)


def xform_tl_log(input='data/tlLog.csv', output='tl.csv'):
    """Transform triple lift log into occurance by unique os,browser,domain

    Args:
        input: triple lift log csv file
        output: output of transformed log
    """
    browser, os, domain = load_support_data()
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
    tl_log = pd.read_csv(input, dtype=tl_dtype, parse_dates=tl_parse_dates)
    tl_log = tl_log.sort_values(by=sort_cols) \
        .merge(browser, how='left', on='tl_browser_id') \
        .merge(os, how='left', on='tl_os_id') \
        .merge(domain, how='left', on='tl_domain_id')
    tl_log_value_counts = tl_log \
        .drop(['auction_id',
            'timestamp',
            'exchange_supplier_id',
            'an_tag_id',
            'user_id',
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
            'url',
        ], axis=1) \
        .reindex(columns=reindex_cols) \
        .value_counts()

    tl_log_xform = pd.DataFrame(tl_log_value_counts) \
        .reset_index() \
        .sort_values(by=sort_cols)
    tl_log_xform.columns = xform_cols
    tl_log_xform.to_csv(output, index=False)


if __name__ == '__main__':
    fire.Fire({
        'tl': xform_tl_log,
        '3party': xform_client_log,
    })

