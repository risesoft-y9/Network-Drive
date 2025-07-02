package net.risesoft.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.model.AssetsModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataAssetsService;
import net.risesoft.y9.Y9LoginUserHolder;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/service/qrcode")
public class QrCodeController {

	private final DataAssetsService dataAssetsService;
    
    @GetMapping("/getDataByQrCode")
	public Y9Result<AssetsModel> getDataByQrCode(@RequestParam String qrcode, @RequestParam String tenantId) {
    	Y9LoginUserHolder.setTenantId(tenantId);
        return dataAssetsService.getDataByQrCode(qrcode);
    }

}
