# Script to add Swagger @Tag annotations to all controllers

$controllers = @(
    @{File="CartController.java"; Tag="Shopping Cart"; Desc="Cart management - add, update, remove items, clear cart"},
    @{File="CategoryController.java"; Tag="Categories"; Desc="Product category management"},
    @{File="CheckoutController.java"; Tag="Checkout"; Desc="Checkout process, order preview, shipping fee calculation"},
    @{File="OrdersController.java"; Tag="Orders"; Desc="Order management for all user roles"},
    @{File="BuyerOrderController.java"; Tag="Buyer Orders"; Desc="Buyer order operations - view, cancel, return items"},
    @{File="SellerOrderController.java"; Tag="Seller Orders"; Desc="Seller order operations - view, update status, shipping"},
    @{File="AdminOrderController.java"; Tag="Admin Orders"; Desc="Admin order management and statistics"},
    @{File="ProductReviewController.java"; Tag="Product Reviews"; Desc="Product review and rating system"},
    @{File="VoucherController.java"; Tag="Vouchers"; Desc="Voucher and discount code management"},
    @{File="WalletController.java"; Tag="Wallet"; Desc="User wallet and balance management"},
    @{File="RefundController.java"; Tag="Refunds"; Desc="Refund request and processing"},
    @{File="SearchController.java"; Tag="Search"; Desc="Product search, suggestions, trending keywords"},
    @{File="AddressController.java"; Tag="Addresses"; Desc="User address management"},
    @{File="GhnController.java"; Tag="GHN Shipping"; Desc="GHN shipping integration - fees, tracking"},
    @{File="GhnMasterController.java"; Tag="GHN Master Data"; Desc="GHN provinces, districts, wards data"},
    @{File="MoMoPaymentController.java"; Tag="MoMo Payment"; Desc="MoMo payment integration"},
    @{File="ProductAttributeController.java"; Tag="Product Attributes"; Desc="Product variant attributes management"},
    @{File="ProductImageController.java"; Tag="Product Images"; Desc="Product image upload and management"},
    @{File="UserController.java"; Tag="Users"; Desc="User management APIs"},
    @{File="ProfileController.java"; Tag="User Profile"; Desc="User profile view and update"},
    @{File="AdminProductController.java"; Tag="Admin Products"; Desc="Admin product management"},
    @{File="AdminSellerRegistrationController.java"; Tag="Admin Seller Registration"; Desc="Admin seller registration approval"},
    @{File="AdminStatsController.java"; Tag="Admin Statistics"; Desc="Platform statistics and analytics"},
    @{File="AdminVoucherController.java"; Tag="Admin Vouchers"; Desc="Admin voucher management"},
    @{File="CommonImageController.java"; Tag="Image Upload"; Desc="Common image upload endpoints"},
    @{File="ShopImageController.java"; Tag="Shop Images"; Desc="Shop logo and banner upload"},
    @{File="UserImageController.java"; Tag="User Images"; Desc="User avatar upload"},
    @{File="ReviewImageController.java"; Tag="Review Images"; Desc="Review image upload"},
    @{File="GoogleAuthController.java"; Tag="Google OAuth"; Desc="Google OAuth authentication"},
    @{File="FacebookAuthController.java"; Tag="Facebook OAuth"; Desc="Facebook OAuth authentication"},
    @{File="ForgotPasswordController.java"; Tag="Password Reset"; Desc="Password reset via email"},
    @{File="GhnWebhookController.java"; Tag="GHN Webhooks"; Desc="GHN shipping status webhooks"}
)

$basePath = "d:\Proj_Nam4\PBL6_E-Commerce\Ecommerce\src\main\java\com\PBL6\Ecommerce\controller"

foreach ($ctrl in $controllers) {
    $filePath = Join-Path $basePath $ctrl.File
    if (Test-Path $filePath) {
        Write-Host "Processing $($ctrl.File)..." -ForegroundColor Cyan
        
        $content = Get-Content $filePath -Raw
        
        # Check if already has @Tag
        if ($content -match '@Tag\(') {
            Write-Host "  Already has @Tag, skipping" -ForegroundColor Yellow
            continue
        }
        
        # Add imports if not present
        if ($content -notmatch 'io\.swagger\.v3\.oas\.annotations') {
            $importLine = "`nimport io.swagger.v3.oas.annotations.tags.Tag;"
            $content = $content -replace '(import .*?;\s+)(@RestController)', "`$1$importLine`n`$2"
        }
        
        # Add @Tag annotation before @RestController
        $tagAnnotation = "@Tag(name = `"$($ctrl.Tag)`", description = `"$($ctrl.Desc)`")`n"
        $content = $content -replace '(@RestController)', "$tagAnnotation`$1"
        
        # Save file
        $content | Set-Content $filePath -NoNewline
        Write-Host "  Added @Tag annotation" -ForegroundColor Green
    } else {
        Write-Host "File not found: $filePath" -ForegroundColor Red
    }
}

Write-Host "`nDone! All controllers have been annotated." -ForegroundColor Green
