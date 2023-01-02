package com.example.messagetest;
//연락처 모듈 만들기
public class ContactModel {
    private int id;
    private String phoneNo;
    private String name;

    // constructor
    public ContactModel(int id, String name, String phoneNo) {
        this.id = id;
        this.phoneNo = validate(phoneNo);
        this.name = name;
    }

    // 전화번호 확인 국가별로 다르기 때문에
    private String validate(String phone) {

        // creating StringBuilder for both the cases
        StringBuilder case1 = new StringBuilder("+82");
        StringBuilder case2 = new StringBuilder("");

        // 만약 문자열에 "+"가 있는지 호가인
        if (phone.charAt(0) != '+') {
            for (int i = 0; i < phone.length(); i++) {
                // 공백 또는 "-"를 제거
                if (phone.charAt(i) != '-' && phone.charAt(i) != ' ') {
                    case1.append(phone.charAt(i));
                }
            }
            return case1.toString();
        } else {
            for (int i = 0; i < phone.length(); i++) {
                // 공백 또는 "-"를 제거
                if (phone.charAt(i) != '-' || phone.charAt(i) != ' ') {
                    case2.append(phone.charAt(i));
                }
            }
            return case2.toString();
        }

    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}