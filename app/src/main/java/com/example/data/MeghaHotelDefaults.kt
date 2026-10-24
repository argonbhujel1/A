package com.example.data

import com.example.model.FoodCategory
import com.example.model.FoodItem
import com.example.model.PromotionCoupon

object MeghaHotelDefaults {

    val categories = listOf(
        FoodCategory(
            id = "chicken",
            nameEn = "Chicken",
            nameNp = "कुखुराको परिकार",
            sortOrder = 1
        ),
        FoodCategory(
            id = "snacks",
            nameEn = "Snacks",
            nameNp = "खाजा",
            sortOrder = 2
        ),
        FoodCategory(
            id = "momo",
            nameEn = "Momo",
            nameNp = "म:म",
            sortOrder = 3
        ),
        FoodCategory(
            id = "chowmein-noodles",
            nameEn = "Chowmein & Noodles",
            nameNp = "चाउमिन तथा नुडल्स",
            sortOrder = 4
        ),
        FoodCategory(
            id = "rice-meals",
            nameEn = "Rice & Meals",
            nameNp = "भात तथा खाना",
            sortOrder = 5
        ),
        FoodCategory(
            id = "beverages",
            nameEn = "Beverages",
            nameNp = "चिसो तथा पेय पदार्थ",
            sortOrder = 6
        )
    )

    // 12 Real verified menu items from Megha Hotel (meghahotel.vercel.app)
    val initialMenuItems = listOf(
        FoodItem(
            id = "4",
            nameEn = "Chicken Wings",
            nameNp = "चिकेन विंग्स (५ पिस)",
            descriptionEn = "Spicy fried chicken wings 5 pieces",
            descriptionNp = "पिरो तथा क्रिस्पी फ्राई चिकेन विंग्स ५ पिस",
            categoryId = "snacks",
            priceNpr = 500.0,
            imageUrl = "https://meghahotel.vercel.app/static/uploads/menu/chicken-wings.jpg",
            isVeg = false,
            isPopular = true,
            isFeatured = true,
            preparationTimeMinutes = 20
        ),
        FoodItem(
            id = "5",
            nameEn = "Steam Momo (Chicken)",
            nameNp = "चिकेन स्टिम म:म (९ पिस)",
            descriptionEn = "9 pcs freshly steamed chicken momo served with house chutney",
            descriptionNp = "ताजा बाफिएको चिकेन म:म ९ पिस र स्पेशल अचार",
            categoryId = "momo",
            priceNpr = 150.0,
            imageUrl = "https://meghahotel.vercel.app/static/uploads/menu/steam-momo.jpg",
            isVeg = false,
            isPopular = true,
            isFeatured = true,
            preparationTimeMinutes = 15
        ),
        FoodItem(
            id = "6",
            nameEn = "Fry Momo (Veg)",
            nameNp = "भेज फ्राई म:म (९ पिस)",
            descriptionEn = "9 pcs crispy golden fried vegetable momo",
            descriptionNp = "कुरमुरे फ्राई गरिएको तरकारी म:म ९ पिस",
            categoryId = "momo",
            priceNpr = 160.0,
            imageUrl = "https://meghahotel.vercel.app/static/uploads/menu/fry-momo.jpg",
            isVeg = true,
            isPopular = true,
            isFeatured = false,
            preparationTimeMinutes = 15
        ),
        FoodItem(
            id = "7",
            nameEn = "Jhol Momo",
            nameNp = "झोल म:म (९ पिस)",
            descriptionEn = "9 pieces momo immersed in hot spicy sesame-tomato soup",
            descriptionNp = "तातो पिरो तिल र टमाटरको झोलमा ९ पिस म:म",
            categoryId = "momo",
            priceNpr = 160.0,
            imageUrl = "https://meghahotel.vercel.app/static/uploads/menu/jhol-momo.jpg",
            isVeg = false,
            isPopular = true,
            isFeatured = true,
            preparationTimeMinutes = 15
        ),
        FoodItem(
            id = "8",
            nameEn = "Chicken Chowmein",
            nameNp = "चिकेन चाउमिन",
            descriptionEn = "Stir-fried noodles with chicken, vegetables and authentic spices",
            descriptionNp = "चिकेन र ताजा तरकारी मिसाइएको स्वादिलो चाउमिन",
            categoryId = "chowmein-noodles",
            priceNpr = 200.0,
            imageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792026342/megha-hotel/menu/fh1j0oyrgcxkthjmnuj4.jpg",
            isVeg = false,
            isPopular = true,
            isFeatured = true,
            preparationTimeMinutes = 15
        ),
        FoodItem(
            id = "9",
            nameEn = "Veg Chowmein",
            nameNp = "भेज चाउमिन",
            descriptionEn = "Stir-fried noodles with seasonal crunchy vegetables",
            descriptionNp = "मौसमी ताजा तरकारी मिसाइएको भेज चाउमिन",
            categoryId = "chowmein-noodles",
            priceNpr = 150.0,
            imageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792026556/megha-hotel/menu/xmrwmy75lwaorbrfgwcp.jpg",
            isVeg = true,
            isPopular = false,
            isFeatured = false,
            preparationTimeMinutes = 12
        ),
        FoodItem(
            id = "11",
            nameEn = "Thukpa",
            nameNp = "थुक्पा (नूडल सुप)",
            descriptionEn = "Hearty Himalayan noodle soup with vegetables and Nepali spices",
            descriptionNp = "तातो पोषणयुक्त हिमाली नूडल सुप र तरकारी",
            categoryId = "chowmein-noodles",
            priceNpr = 200.0,
            imageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792103110/megha-hotel/menu/ej8duja6do2vwdsyarnu.png",
            isVeg = false,
            isPopular = true,
            isFeatured = false,
            preparationTimeMinutes = 15
        ),
        FoodItem(
            id = "12",
            nameEn = "Fried Rice",
            nameNp = "फ्राईड राईस",
            descriptionEn = "Aromatic fried rice wok-tossed with garden vegetables and seasonings",
            descriptionNp = "ताजा तरकारी र मसलाले भुटेको स्वादिलो भात",
            categoryId = "rice-meals",
            priceNpr = 200.0,
            imageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792103271/megha-hotel/menu/ru3ds1rtps5mdigz9c4a.png",
            isVeg = true,
            isPopular = true,
            isFeatured = false,
            preparationTimeMinutes = 15
        ),
        FoodItem(
            id = "13",
            nameEn = "Coke, Sprite, Fanta",
            nameNp = "कोक, स्प्राइट, फ्यान्टा",
            descriptionEn = "Chilled 250ml soft drinks (Coke / Sprite / Fanta)",
            descriptionNp = "चिसो २५० मिली सफ्ट ड्रिंक (कोक / स्प्राइट / फ्यान्टा)",
            categoryId = "beverages",
            priceNpr = 170.0,
            imageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792103420/megha-hotel/menu/vrxtxsaf5dm3lqvqdigu.jpg",
            isVeg = true,
            isPopular = true,
            isFeatured = false,
            preparationTimeMinutes = 2
        ),
        FoodItem(
            id = "14",
            nameEn = "Chicken Lollipop",
            nameNp = "चिकेन ललिपप (५ पिस)",
            descriptionEn = "Crispy French-cut chicken wingettes deep fried with hot dip",
            descriptionNp = "कुरमुरे चिकेन ललिपप ५ पिस सस सहित",
            categoryId = "chicken",
            priceNpr = 300.0,
            imageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792103497/megha-hotel/menu/khwgiab0iscwhs4skzai.jpg",
            isVeg = false,
            isPopular = false,
            isFeatured = true,
            preparationTimeMinutes = 20
        ),
        FoodItem(
            id = "15",
            nameEn = "Chicken Roast",
            nameNp = "चिकेन रोस्ट (१ पिस)",
            descriptionEn = "Tender spiced chicken roast leg / breast piece",
            descriptionNp = "विशिष्ट मसलाले सेकाइएको नरम चिकेन रोस्ट १ पिस",
            categoryId = "chicken",
            priceNpr = 200.0,
            imageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792103607/megha-hotel/menu/iuynddv1bvdxqb29uqos.jpg",
            isVeg = false,
            isPopular = true,
            isFeatured = true,
            preparationTimeMinutes = 18
        ),
        FoodItem(
            id = "16",
            nameEn = "Sausage",
            nameNp = "ससेज (५ पिस)",
            descriptionEn = "Grilled chicken sausages 5 pieces with chili glaze",
            descriptionNp = "पोलेको चिकेन ससेज ५ पिस",
            categoryId = "chicken",
            priceNpr = 300.0,
            imageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792103713/megha-hotel/menu/ydtemtudejhm4grfcqdp.jpg",
            isVeg = false,
            isPopular = false,
            isFeatured = false,
            preparationTimeMinutes = 10
        )
    )

    val defaultCoupons = listOf(
        PromotionCoupon(
            code = "MEGHA50",
            titleEn = "Rs. 50 Off on orders above Rs. 400",
            titleNp = "रु. ४०० भन्दा माथिको अर्डरमा रु. ५० छुट",
            flatDiscountNpr = 50.0,
            minOrderAmountNpr = 400.0,
            isActive = true
        ),
        PromotionCoupon(
            code = "FESTIVE10",
            titleEn = "10% Off on orders above Rs. 600",
            titleNp = "रु. ६०० भन्दा माथिको अर्डरमा १०% छुट",
            discountPercentage = 10.0,
            minOrderAmountNpr = 600.0,
            isActive = true
        ),
        PromotionCoupon(
            code = "WELCOME",
            titleEn = "Rs. 75 Off for first-time order (Min Rs. 500)",
            titleNp = "पहिलो अर्डरमा रु. ७५ छुट (न्यूनतम रु. ५००)",
            flatDiscountNpr = 75.0,
            minOrderAmountNpr = 500.0,
            isActive = true
        )
    )
}
