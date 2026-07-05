package com.meat.purchase.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.purchase.entity.PurchaseReceipt;
public interface PurchaseReceiptService extends IService<PurchaseReceipt> {
    void confirmReceipt(PurchaseReceipt receipt);
}
