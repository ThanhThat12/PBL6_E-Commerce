# Test GHN API and get correct address codes
# Run: .\check-ghn.ps1

$TOKEN = "0cb9d939-afca-11f0-b040-4e257d8388b4"
$API = "https://dev-online-gateway.ghn.vn/shiip/public-api"

Write-Host "========================================"
Write-Host "   GHN API Test"
Write-Host "========================================"
Write-Host ""

# 1. Check Shop ID
Write-Host "1. Checking Shop ID..." -ForegroundColor Yellow
try {
    $shopResponse = Invoke-RestMethod -Uri "$API/v2/shop/all" `
        -Method GET `
        -Headers @{"Token" = $TOKEN; "Content-Type" = "application/json"}
    
    if ($shopResponse.code -eq 200 -and $shopResponse.data.shops) {
        Write-Host "OK - Shop IDs:" -ForegroundColor Green
        foreach ($shop in $shopResponse.data.shops) {
            Write-Host "   Shop ID: $($shop._id) | Name: $($shop.name)" -ForegroundColor White
        }
        $SHOP_ID = $shopResponse.data.shops[0]._id
        Write-Host "   Update: ghn.shop-id=$SHOP_ID" -ForegroundColor Cyan
    } else {
        Write-Host "ERROR - No shops found" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================"

# 2. Get Provinces
Write-Host "2. Getting provinces..." -ForegroundColor Yellow
try {
    $provinces = Invoke-RestMethod -Uri "$API/master-data/province" `
        -Method GET `
        -Headers @{"Token" = $TOKEN}
    
    if ($provinces.code -eq 200) {
        Write-Host "OK - Found $($provinces.data.Count) provinces" -ForegroundColor Green
        
        # Find Ho Chi Minh (ProvinceID = 202)
        $hcm = $provinces.data | Where-Object { $_.ProvinceID -eq 202 }
        
        if ($hcm) {
            Write-Host "   Found HCM: ProvinceID = $($hcm.ProvinceID), Name = $($hcm.ProvinceName)" -ForegroundColor White
            $PROVINCE_ID = $hcm.ProvinceID
        } else {
            Write-Host "   HCM not found, using first province: $($provinces.data[0].ProvinceName)" -ForegroundColor Yellow
            $PROVINCE_ID = $provinces.data[0].ProvinceID
        }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================"

# 3. Get Districts
if ($PROVINCE_ID) {
    Write-Host "3. Getting districts for province $PROVINCE_ID..." -ForegroundColor Yellow
    try {
        $body = @{province_id = $PROVINCE_ID} | ConvertTo-Json
        
        $districts = Invoke-RestMethod -Uri "$API/master-data/district" `
            -Method POST `
            -Headers @{"Token" = $TOKEN; "Content-Type" = "application/json"} `
            -Body $body
        
        if ($districts.code -eq 200) {
            Write-Host "OK - Found $($districts.data.Count) districts" -ForegroundColor Green
            Write-Host "   First 5 districts:" -ForegroundColor White
            $districts.data | Select-Object -First 5 | ForEach-Object {
                Write-Host "     ID: $($_.DistrictID) | Name: $($_.DistrictName)" -ForegroundColor Gray
            }
            
            # Use first district
            $DISTRICT_ID = $districts.data[0].DistrictID
            Write-Host "   Using: DistrictID = $DISTRICT_ID ($($districts.data[0].DistrictName))" -ForegroundColor White
            Write-Host "   Update: ghn.from-district-id=$DISTRICT_ID" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================"

# 4. Get Wards
if ($DISTRICT_ID) {
    Write-Host "4. Getting wards for district $DISTRICT_ID..." -ForegroundColor Yellow
    try {
        $body = @{district_id = $DISTRICT_ID} | ConvertTo-Json
        
        $wards = Invoke-RestMethod -Uri "$API/master-data/ward" `
            -Method POST `
            -Headers @{"Token" = $TOKEN; "Content-Type" = "application/json"} `
            -Body $body
        
        if ($wards.code -eq 200) {
            Write-Host "OK - Found $($wards.data.Count) wards" -ForegroundColor Green
            Write-Host "   First 5 wards:" -ForegroundColor White
            $wards.data | Select-Object -First 5 | ForEach-Object {
                Write-Host "     Code: $($_.WardCode) | Name: $($_.WardName)" -ForegroundColor Gray
            }
            
            # Use first ward
            $WARD_CODE = $wards.data[0].WardCode
            Write-Host "   Using: WardCode = $WARD_CODE ($($wards.data[0].WardName))" -ForegroundColor White
            Write-Host "   Update: ghn.from-ward-code=$WARD_CODE" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================"
Write-Host "   Summary"
Write-Host "========================================"
Write-Host ""

if ($SHOP_ID -and $DISTRICT_ID -and $WARD_CODE) {
    Write-Host "COPY THESE TO application.properties:" -ForegroundColor Green
    Write-Host ""
    Write-Host "ghn.shop-id=$SHOP_ID" -ForegroundColor White
    Write-Host "ghn.from-district-id=$DISTRICT_ID" -ForegroundColor White
    Write-Host "ghn.from-ward-code=$WARD_CODE" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "FAILED - Check token or network connection" -ForegroundColor Red
}
