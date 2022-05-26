package models

// Declare structs here

// Fruit struct
type Fruit struct {
	Name 		string `json:"name"`
	Calories 	float32 `json:"calories"`
	Carbs 		float32 `json:"carbohydrates_total_g"`
	Chols 		float32 `json:"cholesterol_mg"`
	FatSat 		float32 `json:"fat_saturated_g"`
	FatTotal 	float32 `json:"fat_total_g"`
	Fiber 		float32 `json:"fiber_g"`
	Potassium 	float32 `json:"potassium_mg"`
	Protein 	float32 `json:"protein_g"`
	Serving		float32 `json:"serving_size_g"`
	Sodium 		float32 `json:"sodium_mg"`
	Sugar 		float32 `json:"sugar_g"`
}

// Struct for fruit nutrition info
type FruitNutrition struct {
	Items	[]struct {
		Calories 	float32 `json:"calories"`
		Carbs 		float32 `json:"carbohydrates_total_g"`
		Chols 		float32 `json:"cholesterol_mg"`
		FatSat 		float32 `json:"fat_saturated_g"`
		FatTotal 	float32 `json:"fat_total_g"`
		Fiber 		float32 `json:"fiber_g"`
		Name 		string `json:"name"`
		Potassium 	float32 `json:"potassium_mg"`
		Protein 	float32 `json:"protein_g"`
		Serving 	float32 `json:"serving_size_g"`
		Sodium 		float32 `json:"sodium_mg"`
		Sugar 		float32 `json:"sugar_g"`
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