package experiment;

public class PasswordValidator {

    public static void main(String[] args) {
        //Scanner sc = new Scanner(System.in);

        //System.out.println("パスワードを入力してください:");
		//String password = sc.nextLine();
        String password = args[0];

        if (isValidPassword(password)) {
            System.out.println("OK");
        } else {
            System.out.println("NG");
        }

        //sc.close();
    }

    public static boolean isValidPassword(String password) {
        // 条件1: 長さが6以上
        if (password.length() < 6) {
            return false;
        }

        // 条件2: 英字と数字の両方を含む
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            return false;
        }

        // 条件3: 同じ文字を3つ以上連続で使用しない
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) &&
                password.charAt(i) == password.charAt(i + 2)) {
                return false;
            }
        }

        return true;
    }
}
