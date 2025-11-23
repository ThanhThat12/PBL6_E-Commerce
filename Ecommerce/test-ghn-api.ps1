# Script kiểm tra GHN API và lấy mã địa chỉ chính xác
# Run: .\test-ghn-api.ps1

$GHN_TOKEN = "0cb9d939-afca-11f0-b040-4e257d8388b4"
$GHN_API = "https://dev-online-gateway.ghn.vn/shiip/public-api"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   GHN API Test Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Kiểm tra Shop ID
Write-Host "1. Đang kiểm tra Shop ID..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GHN_API/v2/shop/all" `
        -Method GET `
        -Headers @{
            "Token" = $GHN_TOKEN
            "Content-Type" = "application/json"
        }
    
    if ($response.code -eq 200 -and $response.data.shops) {
        Write-Host "✅ Shop IDs:" -ForegroundColor Green
        foreach ($shop in $response.data.shops) {
            Write-Host "   - Shop ID: $($shop._id) | Name: $($shop.name) | Status: $($shop.status)" -ForegroundColor White
        }
        $SHOP_ID = $response.data.shops[0]._id
        Write-Host ""
        Write-Host "📝 Cập nhật vào application.properties:" -ForegroundColor Cyan
        Write-Host "   ghn.shop-id=$SHOP_ID" -ForegroundColor White
    } else {
        Write-Host "❌ Không tìm thấy shop nào" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Lỗi khi gọi API Shop: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

# 2. Lấy danh sách tỉnh/thành
Write-Host "2. Đang lấy danh sách tỉnh/thành..." -ForegroundColor Yellow
try {
    $provinces = Invoke-RestMethod -Uri "$GHN_API/master-data/province" `
        -Method GET `
        -Headers @{
            "Token" = $GHN_TOKEN
        }
    
    if ($provinces.code -eq 200) {
        Write-Host "✅ Danh sách tỉnh/thành (Top 10):" -ForegroundColor Green
        $provinces.data | Select-Object -First 10 | ForEach-Object {
            Write-Host "   - ID: $($_.ProvinceID) | Name: $($_.ProvinceName)" -ForegroundColor White
        }
        
        # Tim Ho Chi Minh
        $hcm = $provinces.data | Where-Object { $_.ProvinceName -like "*Ho Chi Minh*" -or $_.ProvinceName -like "*HCM*" }
        if ($hcm) {
            Write-Host ""
            Write-Host "Tim thay tinh Ho Chi Minh:" -ForegroundColor Cyan
            Write-Host "   ProvinceID: $($hcm.ProvinceID)" -ForegroundColor White
            $PROVINCE_ID = $hcm.ProvinceID
        }
    }
} catch {
    Write-Host "❌ Lỗi khi lấy danh sách tỉnh: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

# 3. Lấy danh sách quận/huyện của Hồ Chí Minh
if ($PROVINCE_ID) {
    Write-Host "3. Đang lấy danh sách quận/huyện của Hồ Chí Minh..." -ForegroundColor Yellow
    try {
        $body = @{
            province_id = $PROVINCE_ID
        } | ConvertTo-Json
        
        $districts = Invoke-RestMethod -Uri "$GHN_API/master-data/district" `
            -Method POST `
            -Headers @{
                "Token" = $GHN_TOKEN
                "Content-Type" = "application/json"
            } `
            -Body $body
        
        if ($districts.code -eq 200) {
            Write-Host "✅ Danh sách quận/huyện (Top 15):" -ForegroundColor Green
            $districts.data | Select-Object -First 15 | ForEach-Object {
                Write-Host "   - ID: $($_.DistrictID) | Name: $($_.DistrictName)" -ForegroundColor White
            }
            
            # Tim Quan 1
            $district1 = $districts.data | Where-Object { $_.DistrictName -like "*Quan 1*" -or $_.DistrictName -eq "Quan 1" }
            if ($district1) {
                Write-Host ""
                Write-Host "Tim thay Quan 1:" -ForegroundColor Cyan
                Write-Host "   DistrictID: $($district1.DistrictID)" -ForegroundColor White
                $DISTRICT_ID = $district1.DistrictID
                
                Write-Host ""
                Write-Host "Cap nhat vao application.properties:" -ForegroundColor Cyan
                Write-Host "   ghn.from-district-id=$DISTRICT_ID" -ForegroundColor White
            }
        }
    } catch {
        Write-Host "❌ Lỗi khi lấy danh sách quận: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "Bo qua buoc 3 vi khong tim thay ProvinceID" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

# 4. Lấy danh sách phường/xã của Quận 1
if ($DISTRICT_ID) {
    Write-Host "4. Đang lấy danh sách phường/xã của Quận 1..." -ForegroundColor Yellow
    try {
        $body = @{
            district_id = $DISTRICT_ID
        } | ConvertTo-Json
        
        $wards = Invoke-RestMethod -Uri "$GHN_API/master-data/ward" `
            -Method POST `
            -Headers @{
                "Token" = $GHN_TOKEN
                "Content-Type" = "application/json"
            } `
            -Body $body
        
        if ($wards.code -eq 200) {
            Write-Host "✅ Danh sách phường/xã (Top 10):" -ForegroundColor Green
            $wards.data | Select-Object -First 10 | ForEach-Object {
                Write-Host "   - Code: $($_.WardCode) | Name: $($_.WardName)" -ForegroundColor White
            }
            
            # Lay phuong dau tien
            $firstWard = $wards.data | Select-Object -First 1
            if ($firstWard) {
                Write-Host ""
                Write-Host "Phuong dau tien (de test):" -ForegroundColor Cyan
                Write-Host "   WardCode: $($firstWard.WardCode)" -ForegroundColor White
                Write-Host "   WardName: $($firstWard.WardName)" -ForegroundColor White
                $WARD_CODE = $firstWard.WardCode
                
                Write-Host ""
                Write-Host "Cap nhat vao application.properties:" -ForegroundColor Cyan
                Write-Host "   ghn.from-ward-code=$WARD_CODE" -ForegroundColor White
            }
        }
    } catch {
        Write-Host "❌ Lỗi khi lấy danh sách phường: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "Bo qua buoc 4 vi khong tim thay DistrictID" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Hoàn thành kiểm tra!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($SHOP_ID -and $DISTRICT_ID -and $WARD_CODE) {
    Write-Host "TOM TAT - Cap nhat vao application.properties:" -ForegroundColor Green
    Write-Host ""
    Write-Host "ghn.shop-id=$SHOP_ID" -ForegroundColor White
    Write-Host "ghn.from-district-id=$DISTRICT_ID" -ForegroundColor White
    Write-Host "ghn.from-ward-code=$WARD_CODE" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "Vui long kiem tra lai GHN token hoac ket noi mang" -ForegroundColor Yellow
}
