package models

// Declare structs here

// Fruit struct
type Fruit struct {
	Name      string  `json:"name"`
	Calories  float32 `json:"calories,omitempty"`
	Carbs     float32 `json:"carbohydrates_total_g,omitempty"`
	Chols     float32 `json:"cholesterol_mg,omitempty"`
	FatSat    float32 `json:"fat_saturated_g,omitempty"`
	FatTotal  float32 `json:"fat_total_g,omitempty"`
	Fiber     float32 `json:"fiber_g,omitempty"`
	Potassium float32 `json:"potassium_mg,omitempty"`
	Protein   float32 `json:"protein_g,omitempty"`
	Serving   float32 `json:"serving_size_g,omitempty"`
	Sodium    float32 `json:"sodium_mg,omitempty"`
	Sugar     float32 `json:"sugar_g,omitempty"`
	Prices    []Price `json:"prices"`
}

type Price struct {
	Store    string `json:"store"`
	Price    string `json:"price"`
	Quantity string `json:"quantity"`
	Date     string `json:"date"`
}

// Struct for fruit nutrition info
type FruitNutrition struct {
	Items []struct {
		Calories  float32 `json:"calories"`
		Carbs     float32 `json:"carbohydrates_total_g"`
		Chols     float32 `json:"cholesterol_mg"`
		FatSat    float32 `json:"fat_saturated_g"`
		FatTotal  float32 `json:"fat_total_g"`
		Fiber     float32 `json:"fiber_g"`
		Name      string  `json:"name"`
		Potassium float32 `json:"potassium_mg"`
		Protein   float32 `json:"protein_g"`
		Serving   float32 `json:"serving_size_g"`
		Sodium    float32 `json:"sodium_mg"`
		Sugar     float32 `json:"sugar_g"`
	}
}

// User model
type User struct {
	ID        int    `json:"userID"`
	FirstName string `json:"firstName"`
	LastName  string `json:"lastName"`
	Email     string `json:"email"`
	Password  string `json:"password,omitempty"`
}

// Error model
type Error struct {
	Code    string `json:"code,omitempty"`
	Title   string `json:"title,omitempty"`
	Message string `json:"message,omitempty"`
}

type Errors struct {
	Errors []Error `json:"errors"`
}

// Coles Pricing structs
type ColesFruitPrices struct {
	ColesFruitPrices []ColesFruit `json:"colesFruitPrices"`
}

type ColesFruit struct {
	Name     string `json:"name"`
	Price    string `json:"price"`
	Quantity string `json:"quantity"`
	Date     string `json:"date"`
	Store    string `json:"store"`
}
